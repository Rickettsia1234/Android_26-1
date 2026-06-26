package com.example.android_2026_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val TAG = "LifecycleTest"
    var count: Int = 0
    lateinit var textViewCount: TextView//지연 초기화로 나중에 선언 하게 함
    val activityResultLauncher = registerForActivityResult(//콜백 통로 설정하는 함수
        /*
            registerForActivityResult :
                결과를 받기 위한 액티비티를 등록하는 함수
                생명주기에서 현재 액티비티를 지우지 못하게 하기 위함
            ActivityResultContracts :
                액티비티 간 상호작용 시 타입 안정성을 위한 추상화된 계약 인터페이스 집합
                ActivityResultContracts :
                    다른 액티비티를 지정하고, 데이터를 돌려받는 계약 형식
        */
        ActivityResultContracts.StartActivityForResult()
    ) { result ->//다음 화면이 닫히면 해당 블록을 실행함
        if(result.resultCode == RESULT_OK) {
            count = result.data?.getIntExtra("RETURN_COUNT", 0) ?: 0
            //엘비스 연산자를 사용해서 0을 기본값으로 가짐
            textViewCount.text = count.toString()
        }
    }

    fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun changeActivityWithCount() {
        val intent = Intent(this, MainActivity2::class.java)
        //intent에 출발지, 목적지를 넣는다.
        intent.putExtra("INTENT_COUNT", count)
        //이름, 변수를 묶어 맵 자료구조와 유사한 Bundle 자료구조에 넣는다
        activityResultLauncher.launch(intent)
        //intent를 발송한다
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d(TAG, "onCreate")
        textViewCount = findViewById(R.id.textView_count)
        val buttonCount = findViewById<Button>(R.id.button_count)//var 이 아니라 val 사용
        /*
            findViewById : id를 이용해 주소값을 찾아 대입하는 함수
            <컴포넌트 타입> (id) R : 리소스의 약자, 모든 리소스 id : 고유 식별자
            Alt + Enter 로 모듈 가져올 수 있음
         */
        val buttonToast = findViewById<Button>(R.id.button_toast)
        val buttonRandom = findViewById<Button>(R.id.button_random)

        buttonCount.setOnClickListener {//함수의 마지막 인자가 람다식일 경우{} 소괄호 생략 가능
            count++
            textViewCount.text = count.toString()
        }

        buttonToast.setOnClickListener {
            showToastMessage(getString(R.string.toast_message))
        }

        buttonRandom.setOnClickListener {
            changeActivityWithCount()
        }

    }override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}