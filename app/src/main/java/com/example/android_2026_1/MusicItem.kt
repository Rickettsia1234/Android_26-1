package com.example.android_2026_1

import android.net.Uri

data class MusicItem(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri
)