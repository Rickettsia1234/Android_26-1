package com.example.android_2026_1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LapAdapter(private val lapList: List<String>) : RecyclerView.Adapter<LapAdapter.LapViewHolder>() {

    class LapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewLap: TextView = itemView as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return LapViewHolder(view)
    }

    override fun onBindViewHolder(holder: LapViewHolder, position: Int) {
        holder.textViewLap.text = lapList[position]
    }

    override fun getItemCount(): Int = lapList.size
}