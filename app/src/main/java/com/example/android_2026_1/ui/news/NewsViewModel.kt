package com.example.android_2026_1.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_2026_1.util.AppLogger
import com.example.android_2026_1.data.NewsItem
import com.example.android_2026_1.data.RetrofitClient
import com.example.android_2026_1.data.SteamAppItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val text: String = "",
    val suggestions: List<SteamAppItem> = emptyList(),
    val newsList: List<NewsItem> = emptyList(),
    val isLoading: Boolean = false,
    val showImg: Boolean = true,
    val dev: Boolean = true,
    val external: Boolean = false,
    val showEmpty: Boolean = true
)

sealed interface NewsEvent {
    data class TextChanged(val text: String) : NewsEvent
    data class SelectItem(val name: String) : NewsEvent
    object Search : NewsEvent
    data class UpdateSettings(
        val showImg: Boolean,
        val dev: Boolean,
        val external: Boolean,
        val showEmpty: Boolean
    ) : NewsEvent
}

class NewsViewModel : ViewModel() {

    companion object {
        private const val TAG = "NewsViewModel"
        private const val FEED_DEV_ANNOUNCEMENT = "steam_community_announcements"
    }

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState = _uiState.asStateFlow()

    private var rawNewsList = emptyList<NewsItem>()

    fun onEvent(event: NewsEvent) {
        when (event) {
            is NewsEvent.TextChanged -> onTextChange(event.text)
            is NewsEvent.SelectItem -> selectItem(event.name)
            is NewsEvent.Search -> doSearch()
            is NewsEvent.UpdateSettings -> updateSettings(
                event.showImg,
                event.dev,
                event.external,
                event.showEmpty
            )
        }
    }

    private fun onTextChange(newText: String) {
        _uiState.update { it.copy(text = newText) }
        if (newText.isEmpty()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        viewModelScope.launch {
            try {
                val result = RetrofitClient.storeService.searchApps(newText)
                _uiState.update {
                    it.copy(suggestions = result.items ?: emptyList())
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error searching apps", e)
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
        }
    }

    private fun selectItem(name: String) {
        _uiState.update { it.copy(text = name, suggestions = emptyList()) }
    }

    private fun doSearch() {
        val query = _uiState.value.text
        if (query.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, suggestions = emptyList()) }

        viewModelScope.launch {
            val appId = findAppId(query)
            rawNewsList = if (appId != null) {
                loadNews(appId)
            } else {
                emptyList()
            }
            applyFilter()
        }
    }

    private suspend fun findAppId(query: String): Int? {
        return try {
            val response = RetrofitClient.storeService.searchApps(query)
            val matchedGame = response.items?.firstOrNull()
            matchedGame?.id
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error finding appId", e)
            null
        }
    }

    private suspend fun loadNews(appId: Int): List<NewsItem> {
        return try {
            val response = RetrofitClient.newsService.getNewsForApp(appId)
            response.appNews?.newsItems ?: emptyList()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error loading news", e)
            emptyList()
        }
    }

    private fun applyFilter() {
        val state = _uiState.value
        val filtered = rawNewsList.filter { item ->
            val isDev = item.feedname == FEED_DEV_ANNOUNCEMENT
            val isEmpty = item.feedname.isNullOrEmpty()
            val isExternal = !isEmpty && !isDev

            (state.dev && isDev) || (state.showEmpty && isEmpty) || (state.external && isExternal)
        }
        _uiState.update { it.copy(newsList = filtered, isLoading = false) }
    }

    private fun updateSettings(showImg: Boolean, dev: Boolean, external: Boolean, showEmpty: Boolean) {
        _uiState.update {
            it.copy(
                showImg = showImg,
                dev = dev,
                external = external,
                showEmpty = showEmpty
            )
        }
        applyFilter()
    }
}