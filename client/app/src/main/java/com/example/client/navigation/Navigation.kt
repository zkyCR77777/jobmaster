package com.example.client.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.client.ui.screens.HomeScreen

private object Screen {
    const val Home = "home"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home,
    ) {
        composable(Screen.Home) {
            HomeScreen()
        }
    }
}
