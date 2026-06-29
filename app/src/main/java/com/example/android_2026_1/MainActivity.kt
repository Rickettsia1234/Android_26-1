package com.example.android_2026_1

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_2026_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val musicList = mutableListOf<MusicItem>()
    private lateinit var adapter: MusicAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setupMusicPlayer()
        } else {
            showPermissionDeniedUI()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts(SCHEME_PACKAGE, packageName, null)
            }
            startActivity(intent)
        }

        checkMusicPermission()
    }

    override fun onResume() {
        super.onResume()
        checkMusicPermission()
    }

    private fun checkMusicPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            setupMusicPlayer()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun setupMusicPlayer() {
        binding.layoutPermissionDenied.visibility = View.GONE
        binding.layoutMusicContainer.visibility = View.VISIBLE

        if (musicList.isEmpty()) {
            loadMusicFiles()
            adapter = MusicAdapter(musicList) { item ->
                startMusicService(item)
                updateBottomPlayer(item)
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = adapter
        }

        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showPermissionDeniedUI() {
        binding.layoutPermissionDenied.visibility = View.VISIBLE
        binding.layoutMusicContainer.visibility = View.GONE
    }

    private fun loadMusicFiles() {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        val query = contentResolver.query(collection, projection, null, null, null)
        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getLong(durationColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                musicList.add(MusicItem(id, title, artist, duration, contentUri))
            }
        }
    }

    private fun startMusicService(item: MusicItem) {
        val intent = Intent(this, MusicService::class.java).apply {
            putExtra(MusicService.EXTRA_TITLE, item.title)
            putExtra(MusicService.EXTRA_ARTIST, item.artist)
            putExtra(MusicService.EXTRA_URI, item.uri.toString())
        }
        ContextCompat.startForegroundService(this, intent)
    }

    private fun updateBottomPlayer(item: MusicItem) {
        binding.tvBottomTitle.text = item.title
        binding.tvBottomArtist.text = item.artist
    }

    companion object {
        const val SCHEME_PACKAGE = "package"
    }
}