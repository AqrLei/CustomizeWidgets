package com.example.storesample.mediastore

import android.net.Uri
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.storesample.databinding.MediaListItemBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaData(
    val id: Long,
    val contentUri: Uri,
    val displayName: String
) : Parcelable

val diffCallback = object : DiffUtil.ItemCallback<MediaData>() {

    override fun areItemsTheSame(oldItem: MediaData, newItem: MediaData): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MediaData, newItem: MediaData): Boolean =
        oldItem == newItem
}

class MediaAdapter(val onClick: (mediaData: MediaData) -> Unit) :
    ListAdapter<MediaData, MediaViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding =
            MediaListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = getItem(position)
        holder.binding.root.tag  = mediaItem
        holder.binding.iv.load(mediaItem.contentUri)
        holder.binding.tvName.text = mediaItem.displayName
    }
}

class MediaViewHolder(
    val binding: MediaListItemBinding,
    val onClick: (mediaData: MediaData) -> Unit
) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.iv.setOnClickListener {
            val mediaData = binding.root.tag as? MediaData ?: return@setOnClickListener
            onClick(mediaData)
        }
    }
}