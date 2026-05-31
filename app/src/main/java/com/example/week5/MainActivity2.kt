package com.example.week5

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity2 : AppCompatActivity() {

    var resultCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val count = intent.getIntExtra("INTENT_COUNT", 0)
        //count 값 수신

        val textGuide = findViewById<TextView>(R.id.textView_guide)
        textGuide.text = getString(R.string.random_message, count)
        //가이트 텍스트 지정

        resultCount = Random.nextInt(0, count + 1)

        val textResult = findViewById<TextView>(R.id.textView_random_result)
        textResult.text = resultCount.toString()
        //결과 값 지정

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            //뒤로가기 버튼 감지
            override fun handleOnBackPressed() {
                val intent = Intent()
                intent.putExtra("RETURN_COUNT", resultCount)

                setResult(RESULT_OK, intent)
                finish()
                //현재 화면을 파괴함
            }
        })
    }
}