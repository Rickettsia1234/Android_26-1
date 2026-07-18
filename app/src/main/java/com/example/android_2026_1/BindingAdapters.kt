package com.example.android_2026_1

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import coil.dispose
import coil.load

@BindingAdapter("imageUri")
fun ImageView.setImageUri(uri: String?) {
    if (uri.isNullOrEmpty()) {
        dispose()
        setImageDrawable(null)
    } else {
        load(uri)
    }
}