package com.example.android_2026_1

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_2026_1.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var timeCentiseconds = 0
    private var timeJob: Job? = null

    private var startTime = 0L
    private var pausedTime = 0L

    private lateinit var lapAdapter: LapAdapter
    private val lapList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lapAdapter = LapAdapter(lapList)
        binding.recyclerViewLaps.apply {
            adapter = lapAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        binding.buttonStartPause.setOnClickListener {
            if (timeJob?.isActive == true) pauseTimer() else startTimer()
        }

        binding.buttonLap.setOnClickListener {
            val currentText = getFormattedTime()
            val secondsElapsed = timeCentiseconds / 100
            val lapRecord = getString(R.string.lap_record_format, secondsElapsed, currentText)

            lapList.add(0, lapRecord)
            lapAdapter.notifyItemInserted(0)
            binding.recyclerViewLaps.scrollToPosition(0)
        }

        binding.buttonStop.setOnClickListener {
            resetTimer()
            clearLapRecords()
        }
    }

    private fun startTimer() {
        startTime = SystemClock.elapsedRealtime() - pausedTime

        timeJob = lifecycleScope.launch {
            binding.buttonStartPause.text = getString(R.string.btn_pause)

            while (isActive) {
                val elapsedMillis = SystemClock.elapsedRealtime() - startTime
                timeCentiseconds = (elapsedMillis / 10).toInt()

                updateTimerText()
                delay(10L)
            }
        }
    }

    private fun pauseTimer() {
        timeJob?.cancel()
        pausedTime = SystemClock.elapsedRealtime() - startTime
        binding.buttonStartPause.text = getString(R.string.btn_start)
    }

    private fun resetTimer() {
        timeJob?.cancel()
        timeCentiseconds = 0
        pausedTime = 0L
        updateTimerText()
        binding.buttonStartPause.text = getString(R.string.btn_start)
    }

    private fun getFormattedTime(): String {
        val m = (timeCentiseconds / 6000).toString().padStart(2, '0')
        val s = ((timeCentiseconds % 6000) / 100).toString().padStart(2, '0')
        val ms = (timeCentiseconds % 100).toString().padStart(2, '0')

        return "$m:$s:$ms"
    }

    private fun updateTimerText() {
        val m = (timeCentiseconds / 6000).toString().padStart(2, '0')
        val s = ((timeCentiseconds % 6000) / 100).toString().padStart(2, '0')
        val ms = (timeCentiseconds % 100).toString().padStart(2, '0')

        binding.textViewTime.text = "$m : $s : $ms"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearLapRecords() {
        lapList.clear()
        lapAdapter.notifyDataSetChanged()
    }
}