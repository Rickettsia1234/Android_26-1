package com.example.android_2026_1

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WordItem(
    val id: Int,
    val word: String,
    val meaning: String,
    val imageUri: String? = null
) : Parcelable