package com.example.storesample

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storesample.databinding.GalleryLayoutBinding

val diffCallback = object : DiffUtil.ItemCallback<Uri>() {

    override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean =
        oldItem.path == newItem.path


    override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean =
        oldItem == newItem

}


class GalleryAdapter(val onClick: (uri: Uri) -> Unit) :
    ListAdapter<Uri, ImageViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            GalleryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = getItem(position)
        holder.binding.root.tag = uri
        holder.binding.iv.load(uri)
    }
}

class ImageViewHolder(val binding: GalleryLayoutBinding, val onClick: (uri: Uri) -> Unit) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.iv.setOnClickListener {
            val uri = binding.root.tag as? Uri ?: return@setOnClickListener
            onClick(uri)
        }
    }
}