package com.example.storesample.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AppUtil {
    fun goToSetting(context: Context, packageName: String) {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${packageName}")
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            context.startActivity(intent)
        }
    }
}