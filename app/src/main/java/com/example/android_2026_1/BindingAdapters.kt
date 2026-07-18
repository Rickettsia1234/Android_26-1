package com.example.android_2026_1

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.load

@BindingAdapter("imageUri")
fun loadImage(imageView: ImageView, uri: String?) {
    if (uri != null) {
        imageView.load(uri)
    } else {
        imageView.setImageDrawable(null)
    }
}