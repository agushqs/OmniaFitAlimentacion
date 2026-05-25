package com.example.omniafitalimentacion.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.omniafitalimentacion.ui.screens.DietScreen
import com.example.omniafitalimentacion.ui.screens.FoodSearchScreen
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

@Composable
fun AppNavigation(viewModel: DietViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "diet_screen" // Pantalla de inicio
    ) {
        // Ruta para la pantalla principal de dietas
        composable("diet_screen") {
            DietScreen(
                viewModel = viewModel,
                onNavigateToSearch = { navController.navigate("search_screen") }
            )
        }

        // Ruta para el buscador de alimentos
        composable("search_screen") {
            FoodSearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() } // <-- Instrucción para volver atrás
            )
        }
    }
}