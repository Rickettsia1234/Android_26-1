package com.example.android_2026_1.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed interface Screen {
    object Home : Screen
    object News : Screen
    object Profile : Screen
    object Game : Screen
}

data class MainUiState(
    val currentScreen: Screen = Screen.Home
)

sealed interface MainEvent {
    data class NavigateTo(val screen: Screen) : MainEvent
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.NavigateTo -> navigateTo(event.screen)
        }
    }

    private fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }
}