package com.example.android_2026_1

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android_2026_1.databinding.ActivityMainBinding
import com.example.android_2026_1.databinding.DialogDeleteBinding
import com.example.android_2026_1.databinding.DialogEditBinding

class MainActivity : AppCompatActivity() {

    private val nameList = mutableListOf<String>()
    private lateinit var nameAdapter: NameAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        nameAdapter = NameAdapter(
            nameList,
            onItemClick = { position -> showDeleteDialog(position) },
            onItemLongClick = { position -> showEditDialog(position) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = nameAdapter

        binding.floatingActionButton.setOnClickListener {
            val inputName = binding.editTextText.text.toString()
            if (inputName.isNotEmpty()) {
                nameList.add(inputName)
                nameAdapter.notifyItemInserted(nameList.size - 1)
                binding.editTextText.text.clear()
            }
        }
    }

    private fun showDeleteDialog(position: Int) {
        val dialogBinding = DialogDeleteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnDeleteCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnDeleteConfirm.setOnClickListener {
            nameList.removeAt(position)
            nameAdapter.notifyItemRemoved(position)
            nameAdapter.notifyItemRangeChanged(position, nameList.size)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditDialog(position: Int) {
        val dialogBinding = DialogEditBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.etDialogName.setText(nameList[position])
        dialogBinding.etDialogName.requestFocus()
        dialogBinding.etDialogName.selectAll()

        dialogBinding.btnEditCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnEditConfirm.setOnClickListener {
            val updatedName = dialogBinding.etDialogName.text.toString()
            if (updatedName.isNotEmpty()) {
                nameList[position] = updatedName
                nameAdapter.notifyItemChanged(position)
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}