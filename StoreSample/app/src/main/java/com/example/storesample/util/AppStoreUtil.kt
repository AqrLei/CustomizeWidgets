package com.example.storesample.util

import android.content.Context
import java.io.File

/**
 * 应用专属文件， 卸载应用时会删除
 * 无需权限（外部存储 4.4以上）
 */
class AppStoreUtil {

    fun getAppFileDir(context: Context): File = context.getExternalFilesDir(null)?: context.filesDir


    fun getAppCacheDir(context: Context): File = context.externalCacheDir ?: context.cacheDir
}