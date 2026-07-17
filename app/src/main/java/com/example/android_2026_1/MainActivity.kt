package com.example.android_2026_1

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val wordList = mutableListOf<WordItem>()
    private var idCount = 0
    private var selectedItem: WordItem? = null

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
                "ADD" -> {
                    val newItem = WordItem(idCount++, word, meaning, uri)
                    wordList.add(newItem)
                    adapter.notifyItemInserted(wordList.size - 1)
                }
                "EDIT" -> {
                    val id = data?.getIntExtra("id", -1) ?: -1
                    val index = wordList.indexOfFirst { it.id == id }
                    if (index != -1) {
                        val updated = WordItem(id, word, meaning, uri)
                        wordList[index] = updated
                        adapter.notifyItemChanged(index)

                        if (selectedItem?.id == id) {
                            showPreview(updated)
                        }
                    }
                }
            }

            if (wordList.isEmpty()) {
                clearPreview()
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

        clearPreview()

        adapter = WordAdapter(wordList) { clickedItem ->
            showPreview(clickedItem)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
            val intent = Intent(this, WordEditActivity::class.java).apply {
                putExtra("mode", "ADD")
            }
            editLauncher.launch(intent)
        }

        btnEdit.setOnClickListener {
            selectedItem?.let { item ->
                val intent = Intent(this, WordEditActivity::class.java).apply {
                    putExtra("mode", "EDIT")
                    putExtra("item", item)
                }
                editLauncher.launch(intent)
            }
        }

        btnDelete.setOnClickListener {
            selectedItem?.let { item ->
                val index = wordList.indexOfFirst { it.id == item.id }
                if (index != -1) {
                    wordList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    clearPreview()
                }
            }
        }
    }

    private fun showPreview(item: WordItem) {
        selectedItem = item
        topWord.text = item.word
        topMeaning.text = item.meaning
        topImage.load(item.imageUri)
    }

    private fun clearPreview() {
        topWord.text = ""
        topMeaning.text = ""
        topImage.setImageDrawable(null)
        selectedItem = null
    }
}