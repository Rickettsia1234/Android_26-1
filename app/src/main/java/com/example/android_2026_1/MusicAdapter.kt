package com.example.android_2026_1

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android_2026_1.databinding.ItemMusicBinding
import java.util.concurrent.TimeUnit

class MusicAdapter(
    private val musicList: List<MusicItem>,
    private val onItemClick: (MusicItem) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding)
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val item = musicList[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvArtist.text = item.artist

        val minutes = TimeUnit.MILLISECONDS.toMinutes(item.duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(item.duration) % 60
        holder.binding.tvDuration.text = String.format(FORMAT_DURATION, minutes, seconds)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = musicList.size

    companion object {
        const val FORMAT_DURATION = "%02d:%02d"
    }
}