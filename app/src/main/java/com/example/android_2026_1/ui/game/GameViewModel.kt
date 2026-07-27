package com.example.android_2026_1.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_2026_1.data.RetrofitClient
import com.example.android_2026_1.data.SteamAppDetailsData
import com.example.android_2026_1.data.SteamAppItem
import com.example.android_2026_1.data.SteamExtraClient
import com.example.android_2026_1.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class GameCardItem(
    val title: String,
    val content: String
)

data class GameUiState(
    val text: String = GameViewModel.EMPTY_STRING,
    val suggestions: List<SteamAppItem> = emptyList(),
    val isLoading: Boolean = false,
    val headerImage: String? = null,
    val gameTitle: String? = null,
    val detailCards: List<GameCardItem> = emptyList()
)

sealed interface GameEvent {
    data class TextChanged(val text: String) : GameEvent
    data class SelectItem(val name: String) : GameEvent
    object Search : GameEvent
}

class GameViewModel : ViewModel() {

    companion object {
        private const val TAG = "GameViewModel"

        const val EMPTY_STRING = ""
        private const val NO_INFO = "정보 없음"
        private const val PLAYER_UNIT = "명"
        private const val FREE_TO_PLAY = "무료 플레이"
        private const val NO_PRICE_INFO = "가격 정보 없음"
        private const val NO_DESCRIPTION = "설명 없음"
        private const val TITLE_PLAYER_COUNT = "실시간 동시 접속자 수"
        private const val TITLE_PRICE = "가격 및 세일 정보"
        private const val TITLE_DESCRIPTION = "게임 설명"
        private const val TITLE_DEVELOPERS = "개발사"
        private const val TITLE_PUBLISHERS = "배급사"
        private const val TITLE_GENRES = "장르"
        private const val DELIMITER_COMMA = ", "

        private const val LOG_ERROR_SEARCH_APPS = "Error searching apps"
        private const val LOG_ERROR_LOADING_PLAYER_COUNT = "Error loading player count"
        private const val LOG_ERROR_FETCHING_DETAILS = "Error fetching game details"
        private const val LOG_ERROR_FINDING_APP_ID = "Error finding appId"
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.TextChanged -> onTextChange(event.text)
            is GameEvent.SelectItem -> selectItem(event.name)
            is GameEvent.Search -> doSearch()
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
                AppLogger.e(TAG, LOG_ERROR_SEARCH_APPS, e)
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
            try {
                val appId = findAppId(query)
                if (appId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            headerImage = null,
                            gameTitle = null,
                            detailCards = emptyList()
                        )
                    }
                    return@launch
                }

                val result = SteamExtraClient.appDetailsService.getAppDetails(appId)
                val appDetails = result[appId.toString()]
                val details = appDetails?.data

                var playerCount = NO_INFO
                try {
                    val countResponse = SteamExtraClient.statsService.getNumberOfCurrentPlayers(appId)
                    val count = countResponse.response?.playerCount
                    if (count != null) {
                        playerCount = "${NumberFormat.getNumberInstance(Locale.US).format(count)}$PLAYER_UNIT"
                    }
                } catch (e: Exception) {
                    AppLogger.e(TAG, LOG_ERROR_LOADING_PLAYER_COUNT, e)
                }

                if (details != null) {
                    val cards = buildCards(details, playerCount)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            headerImage = details.headerImage,
                            gameTitle = details.name ?: query,
                            detailCards = cards
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            headerImage = null,
                            gameTitle = null,
                            detailCards = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, LOG_ERROR_FETCHING_DETAILS, e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        headerImage = null,
                        gameTitle = null,
                        detailCards = emptyList()
                    )
                }
            }
        }
    }

    private suspend fun findAppId(query: String): Int? {
        return try {
            val response = RetrofitClient.storeService.searchApps(query)
            val game = response.items?.firstOrNull()
            game?.id
        } catch (e: Exception) {
            AppLogger.e(TAG, LOG_ERROR_FINDING_APP_ID, e)
            null
        }
    }

    private fun buildCards(details: SteamAppDetailsData, playerCount: String): List<GameCardItem> {
        val list = mutableListOf<GameCardItem>()

        list.add(GameCardItem(TITLE_PLAYER_COUNT, playerCount))

        val price = when {
            details.isFree == true -> FREE_TO_PLAY
            details.priceOverview != null -> {
                val po = details.priceOverview
                if ((po.discountPercent ?: 0) > 0) {
                    "${po.discountPercent}% 할인 (${po.initialFormatted ?: EMPTY_STRING} -> ${po.finalFormatted ?: EMPTY_STRING})"
                } else {
                    po.finalFormatted ?: NO_PRICE_INFO
                }
            }
            else -> NO_PRICE_INFO
        }
        list.add(GameCardItem(TITLE_PRICE, price))

        val desc = details.shortDescription?.ifBlank { NO_DESCRIPTION } ?: NO_DESCRIPTION
        list.add(GameCardItem(TITLE_DESCRIPTION, desc))

        val devs = details.developers?.joinToString(DELIMITER_COMMA)?.ifBlank { NO_INFO } ?: NO_INFO
        list.add(GameCardItem(TITLE_DEVELOPERS, devs))

        val pubs = details.publishers?.joinToString(DELIMITER_COMMA)?.ifBlank { NO_INFO } ?: NO_INFO
        list.add(GameCardItem(TITLE_PUBLISHERS, pubs))

        val genres = details.genres?.mapNotNull { it.description }?.joinToString(DELIMITER_COMMA)?.ifBlank { NO_INFO } ?: NO_INFO
        list.add(GameCardItem(TITLE_GENRES, genres))

        return list
    }
}