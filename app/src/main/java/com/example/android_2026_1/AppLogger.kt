package com.example.android_2026_1

import android.util.Log

object AppLogger {
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, "$message: ${throwable?.message}", throwable)
    }
}