package com.example.android_2026_1.ui.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_2026_1.util.AppLogger
import com.example.android_2026_1.R
import com.example.android_2026_1.data.RetrofitClient
import com.example.android_2026_1.data.UserProfile
import com.example.android_2026_1.data.XmlParserUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException

@Immutable
data class ProfileUiState(
    val text: String = "",
    val userProfile: UserProfile? = null,
    val profileUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessageResId: Int? = null
)

sealed interface ProfileEvent {
    data class TextChanged(val text: String) : ProfileEvent
    data object Search : ProfileEvent
}

class ProfileViewModel : ViewModel() {

    companion object {
        private const val TAG = "ProfileViewModel"
        private const val BASE_PROFILE_URL = "https://steamcommunity.com/profiles/"
        private const val BASE_CUSTOM_ID_URL = "https://steamcommunity.com/id/"
        private const val PATH_CUSTOM_ID = "steamcommunity.com/id/"
        private const val PATH_PROFILE_ID = "steamcommunity.com/profiles/"
        private const val HTTP_TOO_MANY_REQUESTS = 429
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.TextChanged -> onTextChange(event.text)
            is ProfileEvent.Search -> doSearch()
        }
    }

    private fun onTextChange(newText: String) {
        _uiState.update { it.copy(text = newText) }
    }


    private var searchJob: Job? = null

    private val profileCache = mutableMapOf<String, FetchResult>()

    private fun doSearch() {
        val rawQuery = _uiState.value.text.trim()
        if (rawQuery.isEmpty()) return

        val query = extractIdFromQuery(rawQuery)

        searchJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                userProfile = null,
                profileUrl = null,
                errorMessageResId = null
            )
        }

        if (profileCache.containsKey(query)) {
            val cachedResult = profileCache[query]!!
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userProfile = cachedResult.profile,
                    profileUrl = cachedResult.url,
                    errorMessageResId = cachedResult.errorMessageResId
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            val result = fetchUserProfile(query)
            profileCache[query] = result
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userProfile = result.profile,
                    profileUrl = result.url,
                    errorMessageResId = result.errorMessageResId
                )
            }
        }
    }

    private fun extractIdFromQuery(rawQuery: String): String {
        var cleaned = rawQuery.trim().removeSuffix("/")
        if (cleaned.contains(PATH_CUSTOM_ID)) {
            cleaned = cleaned.substringAfter(PATH_CUSTOM_ID)
        } else if (cleaned.contains(PATH_PROFILE_ID)) {
            cleaned = cleaned.substringAfter(PATH_PROFILE_ID)
        }
        return cleaned.substringAfterLast("/")
    }

    private data class FetchResult(
        val profile: UserProfile? = null,
        val url: String? = null,
        val errorMessageResId: Int? = null
    )

    private suspend fun fetchUserProfile(query: String): FetchResult {
        val isNumeric = query.all { it.isDigit() }

        if (isNumeric) {
            val profileResult = searchByProfileId(query)
            if (profileResult.isTooManyRequests) {
                return FetchResult(errorMessageResId = R.string.error_too_many_requests)
            }
            if (profileResult.profile != null) {
                val url = "$BASE_PROFILE_URL${profileResult.profile.steamId64 ?: query}"
                return FetchResult(profile = profileResult.profile, url = url)
            }

            val customResult = searchByCustomId(query)
            if (customResult.isTooManyRequests) {
                return FetchResult(errorMessageResId = R.string.error_too_many_requests)
            }
            if (customResult.profile != null) {
                val url = "$BASE_CUSTOM_ID_URL$query"
                return FetchResult(profile = customResult.profile, url = url)
            }
        } else {
            val customResult = searchByCustomId(query)
            if (customResult.isTooManyRequests) {
                return FetchResult(errorMessageResId = R.string.error_too_many_requests)
            }
            if (customResult.profile != null) {
                val url = if (customResult.profile.steamId64 != null) {
                    "$BASE_PROFILE_URL${customResult.profile.steamId64}"
                } else {
                    "$BASE_CUSTOM_ID_URL$query"
                }
                return FetchResult(profile = customResult.profile, url = url)
            }

            val profileResult = searchByProfileId(query)
            if (profileResult.isTooManyRequests) {
                return FetchResult(errorMessageResId = R.string.error_too_many_requests)
            }
            if (profileResult.profile != null) {
                val url = "$BASE_PROFILE_URL${profileResult.profile.steamId64 ?: query}"
                return FetchResult(profile = profileResult.profile, url = url)
            }
        }

        return FetchResult(errorMessageResId = R.string.user_not_found)
    }

    private data class ApiResult(
        val profile: UserProfile? = null,
        val isTooManyRequests: Boolean = false
    )

    private suspend fun searchByProfileId(id: String): ApiResult = executeApiCall {
        RetrofitClient.communityService.getUserByProfile(id)
    }

    private suspend fun searchByCustomId(id: String): ApiResult = executeApiCall {
        RetrofitClient.communityService.getUserById(id)
    }

    private suspend fun executeApiCall(
        apiCall: suspend () -> ResponseBody
    ): ApiResult = withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            val xmlString = response.string()
            ApiResult(profile = XmlParserUtils.parseUserInfo(xmlString))
        } catch (e: HttpException) {
            AppLogger.e(TAG, "Error fetching user profile", e)
            if (e.code() == HTTP_TOO_MANY_REQUESTS) {
                ApiResult(isTooManyRequests = true)
            } else {
                ApiResult()
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Error fetching user profile", e)
            ApiResult()
        }
    }
}