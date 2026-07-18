package com.example.android_2026_1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android_2026_1.databinding.ItemWordBinding

class WordAdapter(
    private val onItemClick: (WordItem) -> Unit
) : ListAdapter<WordItem, WordAdapter.ViewHolder>(WordDiffCallback) {

    class ViewHolder(val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemWordBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.item = item
        holder.binding.root.setOnClickListener { onItemClick(item) }
        holder.binding.executePendingBindings()
    }

    companion object WordDiffCallback : DiffUtil.ItemCallback<WordItem>() {
        override fun areItemsTheSame(oldItem: WordItem, newItem: WordItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WordItem, newItem: WordItem): Boolean {
            return oldItem == newItem
        }
    }
}