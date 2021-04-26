package com.example.storesample.util

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.IntentSender
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

/**
 * 可共享的媒体文件 （图片、音频、视频， 下载的文件）
 * 需要 READ_EXTERNAL_STORAGE权限
 * https://developer.android.google.cn/training/data-storage/shared/media
 */
object ShareMediaStoreUtil {

    @WorkerThread
    suspend fun <T> queryAllImages(resolver: ContentResolver, block: (cursor: Cursor) -> T): T? {
        //TODO
        return queryMedia(
            resolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC",
            null,
            3,
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
            null,
            block
        )
    }

    /**
     * @param projection arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME)
     * @param selection "${MediaStore.Video.Media.DURATION} >= ?"
     * @param selectionArgs arrayOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES).toString())
     * @param limit
     * @param offset
     */
    @WorkerThread
    suspend fun <T> queryMedia(
        resolver: ContentResolver,
        @RequiresPermission.Read uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
        limit: Int?,
        offset: Int?,
        cancellationSignal: CancellationSignal?,
        block: (cursor: Cursor) -> T
    ): T? {
        var result: T? = null
        withContext(Dispatchers.IO) {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                val limitOrder = if (limit != null || offset != null) {
                    "LIMIT ${limit ?: -1} ${offset?.let { "OFFSET $it" } ?: ""}"
                } else ""
                resolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "$sortOrder  $limitOrder",
                    cancellationSignal
                )?.use {
                    result = block(it)
                }
            } else {
                val bundle =
                    createSqlQueryBundle(selection, selectionArgs, sortOrder, limit, offset)
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
        limit: Int?,
        offset: Int?
    ): Bundle? {
        if (selection == null && selectionArgs == null && sortOrder == null && limit == null && offset == null) {
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

        if (limit != null) {
            queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        }

        if (offset != null) {
            queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        }

        return queryArgs
    }



    suspend fun createImageMedia(
        contentResolver: ContentResolver,
        mediaFileName: String,
        relativePath: String,
        callback: suspend (output: OutputStream?) -> Boolean
    ) {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        createMedia(contentResolver, imageCollection, mediaFileName, relativePath, callback)
    }


    private suspend fun createMedia(
        contentResolver: ContentResolver,
        @RequiresPermission.Write mediaCollection: Uri,
        mediaFileName: String,
        relativePath: String?,
        callback: suspend (output: OutputStream?) -> Boolean
    ) {
        var isWriteByFile = false
        var file: File? = null
        var uri: Uri? = null

        try {
            val newMedia = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, mediaFileName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    relativePath?.let { put(MediaStore.MediaColumns.RELATIVE_PATH, it) }
                    put(MediaStore.MediaColumns.IS_PENDING, true)
                } else {
                    relativePath?.let {
                        isWriteByFile = true
                        val parentFilePath =
                            Environment.getExternalStorageDirectory().absolutePath + "/" + relativePath
                        file = createMediaFile(parentFilePath, mediaFileName)
                        put(MediaStore.MediaColumns.DATA, file!!.absolutePath)
                    }
                }
            }

            val outputStream = if (isWriteByFile) {
                file?.outputStream()
            } else {
                contentResolver.insert(mediaCollection, newMedia)?.let { destinationUri ->
                    uri = destinationUri
                    contentResolver.openOutputStream(destinationUri, "w")
                }
            }

            outputStream?.use { output ->
                val result = callback(output)
                file?.let {
                    if (result) {
                        contentResolver.insert(mediaCollection, newMedia)
                    }
                }
                uri?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        newMedia.put(MediaStore.MediaColumns.IS_PENDING, false)
                    }
                    contentResolver.update(it, newMedia, null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createMediaFile(parentFilePath: String, mediaFileName: String): File {
        return File(parentFilePath, mediaFileName).createFile()
    }

    private fun File.createFile(): File {
        if (exists()) return this
        parentFile?.takeIf { !it.exists() }?.mkdirs()
        if (!exists()) {
            createNewFile()
        }
        return this
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