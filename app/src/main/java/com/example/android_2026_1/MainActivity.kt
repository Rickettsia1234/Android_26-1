package com.example.android_2026_1

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android_2026_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var count: Int = 0

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            count = result.data?.getIntExtra(EXTRA_RETURN_COUNT, 0) ?: 0
            binding.textViewCount.text = count.toString()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showNotificationWithCount()
        } else {
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonCount.setOnClickListener {
            count++
            binding.textViewCount.text = count.toString()
        }

        binding.buttonToast.setOnClickListener {
            Toast.makeText(this, getString(R.string.toast_message), Toast.LENGTH_SHORT).show()
        }

        binding.buttonRandom.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showNotificationWithCount()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                showNotificationWithCount()
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == ACTION_LAUNCH_MAIN2) {
            val countExtra = intent.getIntExtra(EXTRA_INTENT_COUNT, 0)
            val main2Intent = Intent(this, MainActivity2::class.java).apply {
                putExtra(EXTRA_INTENT_COUNT, countExtra)
            }
            activityResultLauncher.launch(main2Intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotificationWithCount() {
        val notificationBinding = com.example.android_2026_1.databinding.NotificationBinding.inflate(layoutInflater)
        val remoteViews = RemoteViews(packageName, R.layout.notification).apply {
            setTextViewText(notificationBinding.tvTitle.id, getString(R.string.notification_title))
            setTextViewText(notificationBinding.tvContent.id, getString(R.string.notification_content, count))


            val moreIntent = Intent(this@MainActivity, NotificationReceiver::class.java).apply {
                action = ACTION_MORE
                putExtra(EXTRA_INTENT_COUNT, count)
            }
            val morePendingIntent = PendingIntent.getBroadcast(
                this@MainActivity,
                0,
                moreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(notificationBinding.btnMore.id, morePendingIntent)

            val closeIntent = Intent(this@MainActivity, NotificationReceiver::class.java).apply {
                action = ACTION_CLOSE
                putExtra(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
            }
            val closePendingIntent = PendingIntent.getBroadcast(
                this@MainActivity,
                1,
                closeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(notificationBinding.btnClose.id, closePendingIntent)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(remoteViews)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val CHANNEL_ID = "notification_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_MORE = "ACTION_MORE"
        const val ACTION_CLOSE = "ACTION_CLOSE"
        const val ACTION_LAUNCH_MAIN2 = "ACTION_LAUNCH_MAIN2"
        const val EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID"
        const val EXTRA_INTENT_COUNT = "INTENT_COUNT"
        const val EXTRA_RETURN_COUNT = "RETURN_COUNT"
    }
}