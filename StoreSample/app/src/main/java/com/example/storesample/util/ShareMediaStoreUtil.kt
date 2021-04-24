package com.example.storesample.util

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.IntentSender
import android.database.Cursor
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 可共享的媒体文件 （图片、音频、视频， 下载的文件）
 * 需要 READ_EXTERNAL_STORAGE权限
 * https://developer.android.google.cn/training/data-storage/shared/media
 */
object ShareMediaStoreUtil {

    @WorkerThread
    suspend fun <T> queryAllImages(resolver: ContentResolver, block: (cursor: Cursor) -> T): T? {
        return queryMedia(
            resolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC ",
            "5 OFFSET 2",
            null,
            block
        )
    }

    @WorkerThread
    suspend fun <T> queryAllVideo(resolver: ContentResolver, block: (cursor: Cursor) -> T): T? {
        return queryMedia(
            resolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null,
            null,
            null,
            block
        )
    }


    @WorkerThread
    suspend fun <T> queryAllAudio(resolver: ContentResolver, block: (cursor: Cursor) -> T): T? {
        return queryMedia(
            resolver,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null,
            null,
            null,
            block
        )
    }

    @WorkerThread
    suspend fun <T> queryMedia(
        resolver: ContentResolver,
        @RequiresPermission.Read uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
        limitOrder: String?,
        cancellationSignal: CancellationSignal?,
        block: (cursor: Cursor) -> T
    ): T? {
        var result: T? = null
        withContext(Dispatchers.IO) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                resolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "$sortOrder $limitOrder",
                    cancellationSignal
                )?.use {
                    result = block(it)
                }
            } else {
                val bundle = createSqlQueryBundle(selection, selectionArgs, sortOrder, limitOrder)
                resolver.query(uri, projection, bundle, cancellationSignal)?.use {
                    result = block(it)
                }
            }

        }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createSqlQueryBundle(
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
        limitOrder: String?
    ): Bundle? {
        if (selection == null && selectionArgs == null && sortOrder == null && limitOrder == null) {
            return null
        }
        val queryArgs = Bundle()
        if (selection != null) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
        }
        if (selectionArgs != null) {
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
        }
        if (sortOrder != null) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
        }

        ContentResolver.QUERY_ARG_SQL_HAVING
        if (limitOrder != null) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, limitOrder)
        }
        return queryArgs
    }


    suspend fun createImageUri(
        contentResolver: ContentResolver,
        mediaFileName: String
    ): Uri? {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        return createMediaUri(
            contentResolver,
            imageCollection,
            MediaStore.Images.Media.DISPLAY_NAME,
            mediaFileName
        )
    }

    suspend fun createAudioUri(
        contentResolver: ContentResolver,
        mediaFileName: String
    ): Uri? {
        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        return createMediaUri(
            contentResolver,
            audioCollection,
            MediaStore.Audio.Media.DISPLAY_NAME,
            mediaFileName
        )
    }

    suspend fun createVideoUri(
        contentResolver: ContentResolver,
        mediaFileName: String
    ): Uri? {
        val videoCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        return createMediaUri(
            contentResolver,
            videoCollection,
            MediaStore.Video.Media.DISPLAY_NAME,
            mediaFileName
        )
    }

    private suspend fun createMediaUri(
        contentResolver: ContentResolver,
        @RequiresPermission.Write mediaCollection: Uri,
        mediaFileNameKey: String,
        mediaFileName: String
    ): Uri? {
        return withContext(Dispatchers.IO) {
            val newMedia = ContentValues().apply {
                //TODO is_pending
                put(mediaFileNameKey, mediaFileName)
            }
            return@withContext contentResolver.insert(mediaCollection, newMedia)
        }
    }

    suspend fun deleteImageMedia(
        contentResolver: ContentResolver,
        @RequiresPermission.Write mediaUri: Uri,
        mediaId: Long,
        senderCallback: (intentSender: IntentSender) -> Unit,
        errorCallback: ((e: Exception) -> Unit)? = null
    ) {
        val selection = "${MediaStore.Images.Media._ID} = ?"
        val selectionArgs = arrayOf(mediaId.toString())
        deleteMedia(
            contentResolver,
            mediaUri,
            selection,
            selectionArgs,
            senderCallback,
            errorCallback
        )
    }

    suspend fun deleteMedia(
        contentResolver: ContentResolver,
        @RequiresPermission.Write mediaUri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        senderCallback: (intentSender: IntentSender) -> Unit,
        errorCallback: ((e: Exception) -> Unit)? = null
    ): Int = withContext(Dispatchers.IO) {
        try {
            contentResolver.delete(mediaUri, selection, selectionArgs)
        } catch (e: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender?.let {
                    senderCallback(it)
                } ?: errorCallback?.invoke(e)
            }
            -1
        }
    }


    suspend fun updateMedia(
        contentResolver: ContentResolver,
        @RequiresPermission.Write mediaUri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        updateContentValues: ContentValues,
        senderCallback: (intentSender: IntentSender) -> Unit,
        errorCallback: ((e: Exception) -> Unit)? = null
    ): Int = withContext(Dispatchers.IO) {
        try {
            contentResolver.update(mediaUri, updateContentValues, selection, selectionArgs)
        } catch (e: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender?.let {
                    senderCallback(it)
                } ?: errorCallback?.invoke(e)
            }
            -1
        }
    }
}