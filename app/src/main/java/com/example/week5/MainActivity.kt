package com.example.week5

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.week5.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding//binding으로 View를 찾기 위해 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //View 바인딩을 통해 fragment_container(xml 파일 id)에 접근
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {//회전시 재시작 방지
            supportFragmentManager.beginTransaction()//트랜잭션 시작
                .replace(
                    R.id.fragment_container,
                    MainFragment()
                )//fragment_container에 MainFragment 표시
                .commit()//트랜잭션 실행
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 0) {//앞의 Fragment 확인
                    val currentFragment = supportFragmentManager.fragments.lastOrNull()

                    if (currentFragment is MainFragment2) {
                        sendCountData(currentFragment)
                    }

                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
            }
        }
        //Activity와 같은 생명주기를 가지게 하고 뒤로가기 콜백 객체를 변수로 찾아 넣으줌
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun sendCountData(mainFragment2: MainFragment2) {//count 값 보내기
        val receivedCount = mainFragment2.getReceivedCount()
        supportFragmentManager.setFragmentResult("countKey", Bundle().apply {
            putInt("updatedCount", receivedCount)
        })
    }
}