package com.example.week5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.week5.databinding.FragmentMain2Binding
import kotlin.random.Random

class MainFragment2 : Fragment() {

    private lateinit var binding: FragmentMain2Binding
    private var receivedCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMain2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {//FragmentMain에서 받은 값을 가져오기
        super.onViewCreated(view, savedInstanceState)

        receivedCount = arguments?.getInt("count") ?: 0//null 체크 겸해서 기본값 있게 count 가져오기

        binding.textViewGuide.text = getString(R.string.random_message, receivedCount)
        receivedCount = Random.nextInt(0, receivedCount + 1)
        binding.textViewRandomResult.text = receivedCount.toString()

    }

    fun getReceivedCount(): Int {//count값 getter
        return receivedCount
    }
}
