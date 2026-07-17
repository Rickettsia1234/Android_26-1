package com.example.android_2026_1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.android.material.textfield.TextInputEditText

class WordEditActivity : AppCompatActivity() {

    private var imageUri: String? = null
    private lateinit var btnImage: ImageButton

    private val pickLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it.toString()
            btnImage.load(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_word_edit)

        val tvTitle = findViewById<TextView>(R.id.text_title)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        val etWord = findViewById<TextInputEditText>(R.id.et_word)
        val etMeaning = findViewById<TextInputEditText>(R.id.et_meaning)
        btnImage = findViewById(R.id.btn_image)

        val mode = intent.getStringExtra("mode") ?: "ADD"
        var editId = -1

        val isEdit = mode == "EDIT"
        tvTitle.text = getString(if (isEdit) R.string.title_word_edit else R.string.title_word_add)
        btnSubmit.text = getString(if (isEdit) R.string.btn_edit else R.string.btn_add)

        if (isEdit) {
            intent.getParcelableExtra<WordItem>("item")?.let { item ->
                editId = item.id
                etWord.setText(item.word)
                etMeaning.setText(item.meaning)
                imageUri = item.imageUri
                btnImage.load(item.imageUri ?: android.R.drawable.ic_input_add)
            }
        } else {
            imageUri = null
            btnImage.setImageResource(android.R.drawable.ic_input_add)
        }

        btnImage.setOnClickListener {
            pickLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("mode", mode)
                putExtra("id", editId)
                putExtra("word", etWord.text.toString())
                putExtra("meaning", etMeaning.text.toString())
                putExtra("imageUri", imageUri)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}