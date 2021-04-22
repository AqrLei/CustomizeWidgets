package com.example.storesample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.storesample.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val galleryAdapter = GalleryAdapter {
            Snackbar.make(binding.rvLocal, "uri: $it", Snackbar.LENGTH_SHORT).show()
        }

        binding.rvLocal.also {
            it.layoutManager = GridLayoutManager(this, 2)
            it.adapter = galleryAdapter
        }

        binding.tvLoadImage.setOnClickListener {
            if (haveStoragePermission()) {
                queryImages()
            } else {
                requestPermission()
            }
        }
        val actionOpenMultiDocuments = DocumentsUtil.registerOpenMultiDocuments(this) {
            galleryAdapter.submitList(it)
        }
        binding.tvPickImage.setOnClickListener {
            actionOpenMultiDocuments.launch(arrayOf("image/*"))
        }

        viewModel.images.observe(this, Observer {
            galleryAdapter.submitList(it)
        })
    }

    private fun haveStoragePermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        if (!haveStoragePermission()) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    queryImages()
                } else {
                    val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )

                    if (showRationale) {
                        showNoAccess()
                    } else {
                        goToSetting()
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun queryImages() {
        viewModel.loadImages()
    }

    private fun showNoAccess() {
        Snackbar.make(binding.tvLoadImage, "permission refused", Snackbar.LENGTH_SHORT).show()
    }

    private fun goToSetting() {
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }

    }

}