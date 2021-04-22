package com.example.storesample

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private var contentObserver: ContentObserver? = null

    private val _images = MutableLiveData<List<Uri>>()
    val images: LiveData<List<Uri>> get() = _images

    fun loadImages() {
        viewModelScope.launch {
            val imageList = ShareMediaStoreUtil.queryImages(
                getApplication<Application>().contentResolver,
                ::queryImages
            )
            _images.postValue(imageList ?: emptyList())

            if (contentObserver == null) {
                contentObserver = getApplication<Application>().contentResolver.registerObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ) {
                    Log.d("AqrLei", "observer changed")
                    loadImages()
                }
            }
        }
    }

    private fun queryImages(cursor: Cursor): List<Uri> {
        val tempImageUrlList = ArrayList<Uri>()

        var imageIndex = 0
        while (cursor.moveToNext()) {
            val columnCount = cursor.columnCount
            val columnNames = cursor.columnNames

            for (i in 0 until columnCount) {
                val type = cursor.getType(i)
                val name = columnNames[i]

                val value = when (type) {
                    Cursor.FIELD_TYPE_NULL -> "null"

                    Cursor.FIELD_TYPE_INTEGER -> "${cursor.getIntOrNull(i)}"

                    Cursor.FIELD_TYPE_FLOAT -> "${cursor.getFloatOrNull(i)}"

                    Cursor.FIELD_TYPE_STRING -> "${cursor.getStringOrNull(i)}"

                    Cursor.FIELD_TYPE_BLOB -> "blob"

                    else -> "ignore"
                }

                Log.d("AqrLei", "imageIndex: $imageIndex, type: $type, name: $name, value: $value")

            }

            try {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val id = cursor.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                tempImageUrlList += contentUri
            } catch (e: IllegalArgumentException) {
                // ignore
            }
            imageIndex ++
        }

        return tempImageUrlList
    }

    private fun ContentResolver.registerObserver(
        uri: Uri,
        observer: (selfChange: Boolean) -> Unit
    ): ContentObserver {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                observer(selfChange)
            }
        }

        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }

}