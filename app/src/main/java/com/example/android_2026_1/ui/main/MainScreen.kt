package com.example.android_2026_1.ui.main

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android_2026_1.R
import com.example.android_2026_1.ui.game.GameRoute
import com.example.android_2026_1.ui.news.NewsRoute
import com.example.android_2026_1.ui.profile.ProfileRoute

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object News : Screen("news")
    object Profile : Screen("profile")
    object Game : Screen("game")
}

@Composable
fun MainRoute(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNews = { navController.navigate(Screen.News.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToGame = { navController.navigate(Screen.Game.route) }
            )
        }
        composable(Screen.News.route) {
            NewsRoute()
        }
        composable(Screen.Profile.route) {
            ProfileRoute()
        }
        composable(Screen.Game.route) {
            GameRoute()
        }
    }
}

@Composable
private fun HomeScreen(
    onNavigateToNews: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            Button(onClick = onNavigateToNews) {
                Text(text = stringResource(R.string.btn_news))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onNavigateToProfile) {
                Text(text = stringResource(R.string.btn_profile))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onNavigateToGame) {
                Text(text = stringResource(R.string.btn_game))
            }
        }
    }
}