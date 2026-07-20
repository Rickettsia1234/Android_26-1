package com.example.android_2026_1

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NameAdapter(
    private val userList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<NameAdapter.NameViewHolder>() {

    class NameViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NameViewHolder {
        val textView = TextView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(50, 40, 50, 40)
            textSize = 16f
        }
        return NameViewHolder(textView)
    }

    override fun onBindViewHolder(holder: NameViewHolder, position: Int) {
        val name = userList[position]
        holder.textView.text = name
        holder.textView.setOnClickListener {
            onItemClick(name)
        }
    }

    override fun getItemCount(): Int = userList.size
}