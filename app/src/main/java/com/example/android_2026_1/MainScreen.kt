package com.example.android_2026_1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainRoute(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MainScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun MainScreen(
    uiState: MainUiState,
    onEvent: (MainEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState.currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                onNavigateToNews = { onEvent(MainEvent.NavigateTo(Screen.News)) },
                onNavigateToProfile = { onEvent(MainEvent.NavigateTo(Screen.Profile)) },
                modifier = modifier
            )
        }
        is Screen.News -> {
            BackHandler {
                onEvent(MainEvent.NavigateTo(Screen.Home))
            }
            NewsRoute(modifier = modifier)
        }
        is Screen.Profile -> {
            BackHandler {
                onEvent(MainEvent.NavigateTo(Screen.Home))
            }
            ProfileRoute(modifier = modifier)
        }
    }
}

@Composable
private fun HomeScreen(
    onNavigateToNews: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = onNavigateToNews) {
                Text(text = stringResource(R.string.btn_news))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onNavigateToProfile) {
                Text(text = stringResource(R.string.btn_profile))
            }
        }
    }
}