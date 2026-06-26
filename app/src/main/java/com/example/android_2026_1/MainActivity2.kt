package com.example.android_2026_1

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android_2026_1.databinding.ActivityMain2Binding
import kotlin.random.Random

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private var resultCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main2) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val count = intent.getIntExtra(MainActivity.EXTRA_INTENT_COUNT, 0)

        binding.textViewGuide.text = getString(R.string.random_message, count)

        resultCount = Random.nextInt(0, count + 1)

        binding.textViewRandomResult.text = resultCount.toString()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent()
                intent.putExtra(MainActivity.EXTRA_RETURN_COUNT, resultCount)
                setResult(RESULT_OK, intent)
                finish()
            }
        })
    }
}