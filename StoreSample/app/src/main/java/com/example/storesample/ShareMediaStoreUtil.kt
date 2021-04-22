package com.example.storesample

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
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
    suspend fun <T> queryImages(resolver: ContentResolver, block: (cursor: Cursor) -> T): T? {
        return queryMedia(resolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, block)
    }

    @WorkerThread
    suspend fun <T> queryVideo(resolver: ContentResolver, block: (cursor: Cursor) -> T): T? {
        return queryMedia(resolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, block)
    }


    @WorkerThread
    suspend fun <T> queryAudio(resolver: ContentResolver, block: (cursor: Cursor) -> T): T? {
        return queryMedia(resolver, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, block)
    }

    @WorkerThread
    private suspend fun <T> queryMedia(
        resolver: ContentResolver,
        @RequiresPermission.Read uri: Uri,
        block: (cursor: Cursor) -> T
    ): T? {
        var result: T? = null
        withContext(Dispatchers.IO) {
            resolver.query(uri, null, null, null, null, null)
                ?.use {
                    result = block(it)
                }
        }
        return result
    }
}