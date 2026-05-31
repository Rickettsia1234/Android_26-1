package com.example.week5

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.week5.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding//바인딩으로 찾기 위해 선언
    private var count: Int = 0

    override fun onCreateView(//Fragment의 UI 생성 매서드
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCount.setOnClickListener {
            count++
            binding.textViewCount.text = count.toString()
        }

        binding.buttonAlertDialog.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("setTitle")
                .setMessage("setMessage")
                .setPositiveButton("setPositiveButton") { _, _ ->
                    count = 0
                    binding.textViewCount.text = count.toString()
                }
                .setNeutralButton("setNeutralButton") { _, _ ->
                    Toast.makeText(requireContext(), "Toast message", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("setNegativeButton") { _, _ -> }
                .show()
        }

        binding.buttonRandom.setOnClickListener {
            val mainFragment2 = MainFragment2().apply {
                arguments = Bundle().apply {
                    putInt("count", count)
                }
            }
            parentFragmentManager.beginTransaction()//트랜잭션으로 Fragment 전환
                .replace(R.id.fragment_container, mainFragment2)
                .addToBackStack(null)
                .commit()
        }
    }
}