package com.example.android_2026_1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: WordAdapter
    private lateinit var topWord: TextView
    private lateinit var topMeaning: TextView
    private lateinit var topImage: ImageView

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
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        topWord = findViewById(R.id.textView)
        topMeaning = findViewById(R.id.textView2)
        topImage = findViewById(R.id.imageView)

        val btnEdit = findViewById<ImageButton>(R.id.btn_edit)
        val btnDelete = findViewById<ImageButton>(R.id.btn_delete)
        val btnAdd = findViewById<FloatingActionButton>(R.id.btn_add)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = WordAdapter { clickedItem ->
            viewModel.selectItem(clickedItem)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val intent = Intent(this, WordEditActivity::class.java).apply {
                putExtra("mode", MODE_ADD)
            }
            editLauncher.launch(intent)
        }

        btnEdit.setOnClickListener {
            viewModel.selectedWord.value?.let { item ->
                val intent = Intent(this, WordEditActivity::class.java).apply {
                    putExtra("mode", MODE_EDIT)
                    putExtra("item", item)
                }
                editLauncher.launch(intent)
            }
        }

        btnDelete.setOnClickListener {
            viewModel.deleteWord()
        }

        viewModel.wordList.observe(this) { newList ->
            adapter.submitList(newList)
        }

        viewModel.selectedWord.observe(this) { item ->
            updatePreview(item)
        }
    }

    private fun updatePreview(item: WordItem?) {
        if (item != null) {
            topWord.text = item.word
            topMeaning.text = item.meaning
            if (item.imageUri != null) {
                topImage.load(item.imageUri)
            } else {
                topImage.setImageDrawable(null)
            }
        } else {
            topWord.text = ""
            topMeaning.text = ""
            topImage.setImageDrawable(null)
        }
    }

    companion object {
        const val MODE_ADD = "ADD"
        const val MODE_EDIT = "EDIT"
    }
}