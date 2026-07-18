package com.example.android_2026_1

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import coil.load
import coil.dispose
import com.example.android_2026_1.databinding.ActivityWordEditBinding

class WordEditActivity : AppCompatActivity() {

    private var imageUri: String? = null
    private lateinit var binding: ActivityWordEditBinding

    private val pickLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it.toString()
            binding.btnImage.load(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_word_edit)

        val mode = intent.getStringExtra("mode") ?: "ADD"
        var editId = -1
        val isEdit = mode == "EDIT"

        binding.isEdit = isEdit

        if (isEdit) {
            val item = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("item", WordItem::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<WordItem>("item")
            }

            item?.let { item ->
                editId = item.id
                imageUri = item.imageUri
                binding.item = item
                if (item.imageUri == null) {
                    binding.btnImage.dispose()
                    binding.btnImage.setImageResource(android.R.drawable.ic_input_add)
                }
            }
        } else {
            imageUri = null
            binding.btnImage.dispose()
            binding.btnImage.setImageResource(android.R.drawable.ic_input_add)
        }

        binding.btnImage.setOnClickListener {
            pickLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            val wordText = binding.etWord.text.toString().trim()
            val meaningText = binding.etMeaning.text.toString().trim()

            if (wordText.isEmpty() || meaningText.isEmpty()) {
                return@setOnClickListener
            }

            val resultIntent = Intent().apply {
                putExtra("mode", mode)
                putExtra("id", editId)
                putExtra("word", wordText)
                putExtra("meaning", meaningText)
                putExtra("imageUri", imageUri)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}