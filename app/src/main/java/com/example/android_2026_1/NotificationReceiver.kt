package com.example.android_2026_1

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(MainActivity.EXTRA_NOTIFICATION_ID, 0)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        when (intent.action) {
            MainActivity.ACTION_MORE -> {
                val count = intent.getIntExtra(MainActivity.EXTRA_INTENT_COUNT, 0)
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    action = MainActivity.ACTION_LAUNCH_MAIN2
                    putExtra(MainActivity.EXTRA_INTENT_COUNT, count)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                context.startActivity(mainIntent)
            }
            MainActivity.ACTION_CLOSE -> {
            }
        }
    }
}