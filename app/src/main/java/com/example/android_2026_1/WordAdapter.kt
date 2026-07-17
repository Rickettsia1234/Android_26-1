package com.example.android_2026_1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class WordAdapter(
    private val items: List<WordItem>,
    private val onItemClick: (WordItem) -> Unit
) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.iv_item_image)
        val txtWord: TextView = view.findViewById(R.id.tv_item_word)
        val txtMeaning: TextView = view.findViewById(R.id.tv_item_meaning)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.txtWord.text = item.word
        holder.txtMeaning.text = item.meaning
        holder.img.load(item.imageUri)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size
}