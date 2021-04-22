package com.example.storesample

import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts

object DocumentsUtil {

    fun createDocument() {

    }

    fun registerOpenDocument(
        activityResultCaller: ActivityResultCaller,
        callback: (documentUriList: Uri?) -> Unit
    ) = activityResultCaller.registerForActivityResult(ActivityResultContracts.OpenDocument(), ) {
        callback(it)
    }


    fun registerOpenMultiDocuments(
        activityResultCaller: ActivityResultCaller,
        callback: (documentUriList: List<Uri>) -> Unit
    ) =
        activityResultCaller.registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
            callback(it?.filterNotNull() ?: emptyList())
        }

}