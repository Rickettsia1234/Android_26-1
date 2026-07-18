package com.example.android_2026_1

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _wordList = MutableLiveData<List<WordItem>>(emptyList())
    val wordList: LiveData<List<WordItem>> = _wordList

    private val _selectedWord = MutableLiveData<WordItem?>(null)
    val selectedWord: LiveData<WordItem?> = _selectedWord

    private var nextId = 0

    private fun getCurrentList(): MutableList<WordItem> {
        return _wordList.value.orEmpty().toMutableList()
    }

    fun addWord(word: String, meaning: String, uri: String?) {
        val currentList = getCurrentList()
        val newItem = WordItem(nextId++, word, meaning, uri)
        currentList.add(newItem)
        _wordList.value = currentList
    }

    fun editWord(id: Int, word: String, meaning: String, uri: String?) {
        val currentList = getCurrentList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            val updated = WordItem(id, word, meaning, uri)
            currentList[index] = updated
            _wordList.value = currentList
            if (_selectedWord.value?.id == id) {
                _selectedWord.value = updated
            }
        }
    }

    fun deleteWord() {
        val item = _selectedWord.value ?: return
        val currentList = getCurrentList()
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            currentList.removeAt(index)
            _wordList.value = currentList
            clearSelection()
        }
    }

    fun selectItem(item: WordItem) {
        _selectedWord.value = item
    }

    fun clearSelection() {
        _selectedWord.value = null
    }
}