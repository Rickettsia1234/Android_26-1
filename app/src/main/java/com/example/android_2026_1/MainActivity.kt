package com.example.android_2026_1

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_2026_1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: WordAdapter
    private lateinit var binding: ActivityMainBinding

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val mode = data?.getStringExtra("mode")
            val word = data?.getStringExtra("word").orEmpty()
            val meaning = data?.getStringExtra("meaning").orEmpty()
            val uri = data?.getStringExtra("imageUri")

            when (mode) {
                MODE_ADD -> viewModel.addWord(word, meaning, uri)
                MODE_EDIT -> {
                    val id = data?.getIntExtra("id", -1) ?: -1
                    viewModel.editWord(id, word, meaning, uri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = WordAdapter { clickedItem ->
            viewModel.selectItem(clickedItem)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, WordEditActivity::class.java).apply {
                putExtra("mode", MODE_ADD)
            }
            editLauncher.launch(intent)
        }

        binding.btnEdit.setOnClickListener {
            viewModel.selectedWord.value?.let { item ->
                val intent = Intent(this, WordEditActivity::class.java).apply {
                    putExtra("mode", MODE_EDIT)
                    putExtra("item", item)
                }
                editLauncher.launch(intent)
            }
        }

        viewModel.wordList.observe(this) { newList ->
            adapter.submitList(newList)
        }
    }

    companion object {
        const val MODE_ADD = "ADD"
        const val MODE_EDIT = "EDIT"
    }
}