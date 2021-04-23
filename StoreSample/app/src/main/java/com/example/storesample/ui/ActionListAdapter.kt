package com.example.storesample.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.storesample.databinding.ActionListItemBinding

data class Action(@StringRes val nameRes: Int, @IdRes val actionRes: Int)

class ActionListAdapter(private val dataList: Array<Action>) : RecyclerView.Adapter<ActionListViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionListViewHolder {
        val binding = ActionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActionListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActionListViewHolder, position: Int) {
        val context = holder.binding.root.context
        holder.binding.tvAction.text = context.getString(dataList[position].nameRes)
        holder.binding.tvAction.setOnClickListener {
            it.findNavController().navigate(dataList[position].actionRes)
        }
    }

    override fun getItemCount(): Int = dataList.size
}

class ActionListViewHolder(val binding: ActionListItemBinding): RecyclerView.ViewHolder(binding.root)