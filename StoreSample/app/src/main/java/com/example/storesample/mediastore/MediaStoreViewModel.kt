package com.example.storesample.mediastore

import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.IntentSender
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
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
import com.example.storesample.util.ShareMediaStoreUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private const val RANDOM_IMAGE_URL = "https://source.unsplash.com/random/500x500"

class MediaStoreCreateViewModel(application: Application) : AndroidViewModel(application) {
    private val httpClient by lazy { OkHttpClient() }

    var saveInPending = false

    fun saveRandomImageFromInternet() {
        if (saveInPending) return

        saveInPending = true
        viewModelScope.launch {
            val imageUri = createPhotoUri()
            val request = Request.Builder().url(RANDOM_IMAGE_URL).build()

            withContext(Dispatchers.IO) {
                imageUri?.let { destinationUri ->
                    httpClient.newCall(request).execute().body()?.use { responseBody ->

                        getApplication<Application>().contentResolver.openOutputStream(
                            destinationUri,
                            "w"
                        )?.use {
                            responseBody.byteStream().copyTo(it)
                            saveInPending = false
                        }

                    }
                }
            }
        }
    }

    suspend fun createPhotoUri(): Uri? {
        return ShareMediaStoreUtil.createImageUri(
            getApplication<Application>().contentResolver,
            generateFileName("jpg")
        )
    }

    private fun generateFileName(extension: String): String {
        return "${System.currentTimeMillis()}.$extension"
    }
}

class MediaStoreLoadViewModel(application: Application) : AndroidViewModel(application) {

    init {
        getApplication<Application>().contentResolver.registerObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ) {
            Log.d("AqrLei-Image", "observer changed")
            loadImages()
        }
    }

    private val _images = MutableLiveData<List<MediaData>>()
    val images: LiveData<List<MediaData>> get() = _images

    private var audioContentObserver: ContentObserver? = null
    private val _audios = MutableLiveData<List<MediaData>>()
    val audios: LiveData<List<MediaData>> get() = _audios

    private var videoContentObserver: ContentObserver? = null
    private val _videos = MutableLiveData<List<MediaData>>()
    val videos: LiveData<List<MediaData>> get() = _videos


    fun loadImages() {
        viewModelScope.launch {
            val imageList = ShareMediaStoreUtil.queryImages(
                getApplication<Application>().contentResolver,
                ::queryImages
            )
            _images.postValue(imageList ?: emptyList())
        }
    }

    fun loadAudio() {
        viewModelScope.launch {
            val audioList = ShareMediaStoreUtil.queryAudio(
                getApplication<Application>().contentResolver,
                ::queryAudios
            )

            _audios.postValue(audioList ?: emptyList())
            if (audioContentObserver == null) {
                audioContentObserver =
                    getApplication<Application>().contentResolver.registerObserver(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    ) {

                        Log.d("AqrLei-Audio", "observer changed")
                        loadAudio()
                    }
            }
        }
    }

    fun loadVideo() {

        viewModelScope.launch {
            val videoList = ShareMediaStoreUtil.queryVideo(
                getApplication<Application>().contentResolver,
                ::queryVideos
            )

            _videos.postValue(videoList ?: emptyList())
            if (videoContentObserver == null) {
                audioContentObserver =
                    getApplication<Application>().contentResolver.registerObserver(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    ) {
                        Log.d("AqrLei-Video", "observer changed")
                        loadVideo()
                    }
            }
        }
    }

    private fun queryImages(cursor: Cursor): List<MediaData> {
        val tempImageMediaList = ArrayList<MediaData>()

        var imageIndex = 0
        while (cursor.moveToNext()) {
            val columnCount = cursor.columnCount
            for (i in 0 until columnCount) {
                log("Image", imageIndex, i, cursor)
            }

            try {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                tempImageMediaList += MediaData(id, contentUri, name ?: "--")
            } catch (e: IllegalArgumentException) {
                // ignore
            }
            imageIndex++
        }

        return tempImageMediaList
    }

    private fun queryAudios(cursor: Cursor): List<MediaData> {
        val tempAudioMediaList = ArrayList<MediaData>()
        var audioIndex = 0
        while (cursor.moveToNext()) {
            val columnCount = cursor.columnCount

            for (i in 0 until columnCount) {
                log("Audio", audioIndex, i, cursor)
            }

            try {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val id = cursor.getLong(idColumn)
                val name = cursor.getStringOrNull(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                )
                tempAudioMediaList += MediaData(id, contentUri, name ?: "--")
            } catch (e: IllegalArgumentException) {
                //ignore
            }

            audioIndex++
        }

        return tempAudioMediaList
    }

    private fun queryVideos(cursor: Cursor): List<MediaData> {
        val tempVideoMediaList = ArrayList<MediaData>()
        var audioIndex = 0
        while (cursor.moveToNext()) {
            val columnCount = cursor.columnCount

            for (i in 0 until columnCount) {
                log("Video", audioIndex, i, cursor)
            }

            try {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val id = cursor.getLong(idColumn)
                val name = cursor.getStringOrNull(displayNameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                )
                tempVideoMediaList += MediaData(id, contentUri, name ?: "--")
            } catch (e: IllegalArgumentException) {
                //ignore
            }

            audioIndex++
        }

        return tempVideoMediaList
    }

    private fun log(mediaType: String, index: Int, i: Int, cursor: Cursor) {
        val type = cursor.getType(i)
        val name = cursor.columnNames[i]

        val value = when (type) {
            Cursor.FIELD_TYPE_NULL -> "null"

            Cursor.FIELD_TYPE_INTEGER -> "${cursor.getIntOrNull(i)}"

            Cursor.FIELD_TYPE_FLOAT -> "${cursor.getFloatOrNull(i)}"

            Cursor.FIELD_TYPE_STRING -> "${cursor.getStringOrNull(i)}"

            Cursor.FIELD_TYPE_BLOB -> "blob"

            else -> "ignore"
        }

        Log.d(
            "AqrLei-$mediaType",
            "index: $index, type: $type, name: $name, value: $value"
        )
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

class MediaStoreDeleteViewModel(application: Application) : AndroidViewModel(application) {

    private var pendingDeleteImage: MediaData? = null
    private val _permissionNeededForDelete = MutableLiveData<IntentSender?>()
    val permissionNeededForDelete: LiveData<IntentSender?> get() = _permissionNeededForDelete
    fun deleteImage(image: MediaData) {
        viewModelScope.launch {
            performDeleteImage(image)
        }
    }

    fun deletePendingImage() {
        pendingDeleteImage?.let { image ->
            pendingDeleteImage = null
            deleteImage(image)
        }
    }

    private suspend fun performDeleteImage(image: MediaData) =
        getApplication<Application>().contentResolver.let {
            ShareMediaStoreUtil.deleteImageMedia(it, image.contentUri, image.id, { intentSender ->
                pendingDeleteImage = image
                _permissionNeededForDelete.postValue(intentSender)
            })
        }

}