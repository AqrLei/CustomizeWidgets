package com.example.storesample.saf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.storesample.util.DocumentsUtil
import com.example.storesample.GalleryAdapter
import com.example.storesample.databinding.FragmentSafBinding
import com.google.android.material.snackbar.Snackbar

class SAFFragment : Fragment() {
    private var _binding: FragmentSafBinding? = null

    private val binding get() = _binding!!

    private val galleryAdapter = GalleryAdapter {
        Snackbar.make(binding.rvLocal, "uri: $it", Snackbar.LENGTH_SHORT).show()
    }

    private val actionOpenMultiDocuments = DocumentsUtil.registerOpenMultiDocuments(this) {
        galleryAdapter.submitList(it.toMutableList())
    }

    private val actionOpenDocument = DocumentsUtil.registerOpenDocument(this) {
        galleryAdapter.submitList(if (it == null) null else mutableListOf(it))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvLocal.also {
            it.layoutManager = GridLayoutManager(requireContext(), 2)
            it.adapter = galleryAdapter
        }

        binding.tvLoadImage.setOnClickListener {
            actionOpenMultiDocuments.launch(arrayOf("image/*"))
        }
        binding.tvPickImage.setOnClickListener {
            actionOpenDocument.launch(arrayOf("image/*"))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}