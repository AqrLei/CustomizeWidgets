package com.example.storesample.mediastore

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.storesample.GalleryAdapter
import com.example.storesample.databinding.FragmentMediaStoreBinding
import com.example.storesample.util.ShareMediaStoreUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MediaStoreFragment : Fragment() {

    private var _binding: FragmentMediaStoreBinding? = null

    private val binding get() = _binding!!

    private val loadViewModel: MediaStoreLoadViewModel by viewModels()

    private val createViewModel: MediaStoreCreateViewModel by viewModels()

    private val actionIntentSender =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            Log.d("AqrLei", "resultCode: ${it.resultCode}")
            if (it.resultCode == Activity.RESULT_OK) {

            }
        }

    private val actionRequestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.isNotEmpty()) {
                when {
                    it.getOrElse(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        { false }) -> queryImages()
                    it.getOrElse(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        { false }) -> createImages()

                    else -> {

                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
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
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val galleryAdapter = GalleryAdapter {
            lifecycleScope.launch {
                val result = ShareMediaStoreUtil.deleteMedia(
                    requireContext().contentResolver,
                    it, null, null
                ) { intentSender ->
                    actionIntentSender.launch(IntentSenderRequest.Builder(intentSender)
                        .build())
                }

                val message =
                    if (result > 0) "uri: $it delete successful" else "uri: $it delete failure"

                Snackbar.make(binding.rvLocal, message, Snackbar.LENGTH_SHORT).show()


            }
        }

        binding.rvLocal.also {
            it.layoutManager = GridLayoutManager(requireContext(), 2)
            it.adapter = galleryAdapter
        }

        binding.tvLoadImage.setOnClickListener {
            if (haveStoragePermission()) {
                queryImages()
            } else {
                actionRequestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }

        binding.tvDownloadImage.setOnClickListener {
            if (checkMediaStorePermission()) {
                createImages()
            } else {
                actionRequestPermission.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        }

        loadViewModel.images.observe(viewLifecycleOwner, Observer {
            galleryAdapter.submitList(it?.toMutableList()) {

                // another new way?
                galleryAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun createImages() {
        createViewModel.saveRandomImageFromInternet()
    }

    private fun queryImages() {
        loadViewModel.loadImages()
    }

    private fun showNoAccess() {
        Snackbar.make(binding.tvLoadImage, "permission refused", Snackbar.LENGTH_SHORT).show()
    }

    private fun goToSetting() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${requireContext().packageName}")
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }

    private fun haveStoragePermission(): Boolean = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    private fun checkMediaStorePermission(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) true else
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}