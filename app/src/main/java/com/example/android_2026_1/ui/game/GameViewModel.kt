package com.example.android_2026_1.ui.game

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_2026_1.R
import com.example.android_2026_1.data.RetrofitClient
import com.example.android_2026_1.data.SteamAppDetailsData
import com.example.android_2026_1.data.SteamAppItem
import com.example.android_2026_1.data.SteamExtraClient
import com.example.android_2026_1.util.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val args: Any
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }
}

@Immutable
data class GameCardItem(
    @param:StringRes val titleRes: Int,
    val content: UiText
)

@Immutable
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
    data object Search : GameEvent
}

class GameViewModel : ViewModel() {

    companion object {
        private const val TAG = "GameViewModel"

        const val EMPTY_STRING = ""
        private const val DELIMITER_COMMA = ", "
        private const val SEARCH_DEBOUNCE_MILLIS = 300L

        private const val LOG_ERROR_SEARCH_APPS = "앱 검색 중 오류 발생"
        private const val LOG_ERROR_LOADING_PLAYER_COUNT = "동시 접속자 수 로딩 중 오류 발생"
        private const val LOG_ERROR_FETCHING_DETAILS = "게임 상세 정보 조회 중 오류 발생"
        private const val LOG_ERROR_FINDING_APP_ID = "앱 ID 검색 중 오류 발생"
    }

    private data class GameSearchResultData(
        val headerImage: String?,
        val gameTitle: String?,
        val detailCards: List<GameCardItem>
    )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val gameCache = mutableMapOf<String, GameSearchResultData>()

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.TextChanged -> onTextChange(event.text)
            is GameEvent.SelectItem -> selectItem(event.name)
            is GameEvent.Search -> doSearch()
        }
    }

    private fun onTextChange(newText: String) {
        _uiState.update { it.copy(text = newText) }

        searchJob?.cancel()

        if (newText.isEmpty()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            try {
                val result = RetrofitClient.storeService.searchApps(newText)
                _uiState.update {
                    it.copy(suggestions = result.items ?: emptyList())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLogger.e(TAG, LOG_ERROR_SEARCH_APPS, e)
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
        }
    }

    private fun selectItem(name: String) {
        _uiState.update { it.copy(text = name, suggestions = emptyList()) }
        doSearch()
    }

    private fun doSearch() {
        val query = _uiState.value.text.trim()
        if (query.isEmpty()) return

        searchJob?.cancel()

        if (gameCache.containsKey(query)) {
            val cached = gameCache[query]!!
            _uiState.update {
                it.copy(
                    isLoading = false,
                    suggestions = emptyList(),
                    headerImage = cached.headerImage,
                    gameTitle = cached.gameTitle,
                    detailCards = cached.detailCards
                )
            }
            return
        }

        val cachedAppId = _uiState.value.suggestions.firstOrNull {
            it.name.equals(query, ignoreCase = true)
        }?.id

        _uiState.update { it.copy(isLoading = true, suggestions = emptyList()) }

        searchJob = viewModelScope.launch {
            try {
                val appId = cachedAppId ?: findAppId(query)
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

                var playerCount: UiText = UiText.StringResource(R.string.no_info)
                try {
                    val countResponse = SteamExtraClient.statsService.getNumberOfCurrentPlayers(appId)
                    val count = countResponse.response?.playerCount
                    if (count != null) {
                        val formattedCount = NumberFormat.getNumberInstance(Locale.US).format(count)
                        playerCount = UiText.StringResource(R.string.player_unit, formattedCount)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    AppLogger.e(TAG, LOG_ERROR_LOADING_PLAYER_COUNT, e)
                }

                if (details != null) {
                    val cards = buildCards(details, playerCount)
                    val searchResultData = GameSearchResultData(
                        headerImage = details.headerImage,
                        gameTitle = details.name ?: query,
                        detailCards = cards
                    )
                    gameCache[query] = searchResultData

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            headerImage = searchResultData.headerImage,
                            gameTitle = searchResultData.gameTitle,
                            detailCards = searchResultData.detailCards
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
            } catch (e: CancellationException) {
                throw e
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.e(TAG, LOG_ERROR_FINDING_APP_ID, e)
            null
        }
    }

    private fun buildCards(details: SteamAppDetailsData, playerCount: UiText): List<GameCardItem> {
        val list = mutableListOf<GameCardItem>()

        list.add(GameCardItem(R.string.title_player_count, playerCount))

        val price: UiText = when {
            details.isFree == true -> UiText.StringResource(R.string.free_to_play)
            details.priceOverview != null -> {
                val po = details.priceOverview
                if ((po.discountPercent ?: 0) > 0) {
                    UiText.StringResource(
                        R.string.discount_format,
                        po.discountPercent ?: 0,
                        po.initialFormatted ?: EMPTY_STRING,
                        po.finalFormatted ?: EMPTY_STRING
                    )
                } else {
                    po.finalFormatted?.let { UiText.DynamicString(it) } ?: UiText.StringResource(R.string.no_price_info)
                }
            }
            else -> UiText.StringResource(R.string.no_price_info)
        }
        list.add(GameCardItem(R.string.title_price, price))

        val desc: UiText = if (!details.shortDescription.isNullOrBlank()) {
            UiText.DynamicString(details.shortDescription)
        } else {
            UiText.StringResource(R.string.no_description)
        }
        list.add(GameCardItem(R.string.title_description, desc))

        val devsString = details.developers?.joinToString(DELIMITER_COMMA)
        val devs: UiText = if (!devsString.isNullOrBlank()) {
            UiText.DynamicString(devsString)
        } else {
            UiText.StringResource(R.string.no_info)
        }
        list.add(GameCardItem(R.string.title_developers, devs))

        val pubsString = details.publishers?.joinToString(DELIMITER_COMMA)
        val pubs: UiText = if (!pubsString.isNullOrBlank()) {
            UiText.DynamicString(pubsString)
        } else {
            UiText.StringResource(R.string.no_info)
        }
        list.add(GameCardItem(R.string.title_publishers, pubs))

        val genresString = details.genres?.mapNotNull { it.description }?.joinToString(DELIMITER_COMMA)
        val genres: UiText = if (!genresString.isNullOrBlank()) {
            UiText.DynamicString(genresString)
        } else {
            UiText.StringResource(R.string.no_info)
        }
        list.add(GameCardItem(R.string.title_genres, genres))

        return list
    }
}