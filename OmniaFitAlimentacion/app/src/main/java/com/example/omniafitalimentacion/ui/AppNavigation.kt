package com.example.omniafitalimentacion.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.omniafitalimentacion.model.TipoComida
import com.example.omniafitalimentacion.ui.screens.DietScreen
import com.example.omniafitalimentacion.ui.screens.FoodSearchScreen
import com.example.omniafitalimentacion.ui.screens.MealDetailScreen
import com.example.omniafitalimentacion.ui.screens.OmniaFitBottomNavigation
import com.example.omniafitalimentacion.ui.screens.OmniaFitTopBar
import com.example.omniafitalimentacion.ui.screens.WelcomeScreen
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

// Aquí definimos el "grafo" de la app: qué pantallas existen y cómo se enlazan.
// Cada pantalla es un nodo (composable) identificado por una ruta (String tipo URL).
@Composable
fun AppNavigation(viewModel: DietViewModel) {
    // El NavController es el "controlador del mando a distancia": lo usamos para
    // pedir navegaciones (navigate, popBackStack...) desde cualquier pantalla.
    val navController = rememberNavController()

    // collectAsState convierte el StateFlow en un State<T> observable por Compose:
    // cuando el ViewModel cambia el valor, esta función se recompone sola.
    val rutaInicial by viewModel.rutaInicial.collectAsState()
    val ruta = rutaInicial

    if (ruta == null) {
        // Estado de carga inicial: el ViewModel todavía está mirando la BD para
        // decidir si arrancamos en welcome o en las comidas. Son apenas unos ms,
        // pero si no pintamos nada aquí veríamos un parpadeo en blanco. Mostramos
        // el marco (TopBar/BottomBar) ya, así la transición es visualmente
        // continua: al usuario le parece que la app simplemente "cargó".
        Scaffold(
            topBar = { OmniaFitTopBar() },
            bottomBar = { OmniaFitBottomNavigation() }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues))
        }
        return
    }

    // Cuando ya sabemos la ruta inicial, construimos el grafo de navegación. Esto
    // ocurre una sola vez por sesión (el NavHost no se reconstruye al navegar).
    NavHost(
        navController = navController,
        startDestination = ruta
    ) {
        composable("welcome_screen") {
            WelcomeScreen(
                onAddDietClick = {
                    // crearNuevaDieta es idempotente: solo crea si no había ninguna.
                    viewModel.crearNuevaDieta("Dieta Base", "2026-05-25")
                    navController.navigate("diet_screen") {
                        // popUpTo borra welcome del back stack al navegar. Sin esto,
                        // si el usuario pulsa "atrás" desde las comidas volvería a
                        // welcome (raro). Con esto, "atrás" desde las comidas
                        // simplemente cierra la app.
                        popUpTo("welcome_screen") { inclusive = true }
                    }
                }
            )
        }

        composable("diet_screen") {
            DietScreen(
                viewModel = viewModel,
                onNavigateToMealDetail = { tipo ->
                    // El nombre del enum (DESAYUNO, ALMUERZO...) viaja como parte
                    // de la "URL": p.ej. meal_detail/DESAYUNO
                    navController.navigate("meal_detail/${tipo.name}")
                }
            )
        }

        composable(
            // Las llaves {tipoComida} marcan un argumento dinámico en la ruta.
            route = "meal_detail/{tipoComida}",
            arguments = listOf(navArgument("tipoComida") { type = NavType.StringType })
        ) { backStackEntry ->
            // Sacamos el String del argumento y lo convertimos otra vez al enum.
            // runCatching evita que la app crashee si por lo que sea llega un
            // valor inesperado: si valueOf falla, devolvemos DESAYUNO por defecto.
            val tipoStr = backStackEntry.arguments?.getString("tipoComida")
                ?: TipoComida.DESAYUNO.name
            val tipo = runCatching { TipoComida.valueOf(tipoStr) }
                .getOrDefault(TipoComida.DESAYUNO)
            MealDetailScreen(
                viewModel = viewModel,
                tipoComida = tipo,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSearch = {
                    navController.navigate("search_screen/${tipo.name}")
                }
            )
        }

        composable(
            route = "search_screen/{tipoComida}",
            arguments = listOf(navArgument("tipoComida") { type = NavType.StringType })
        ) { backStackEntry ->
            val tipoStr = backStackEntry.arguments?.getString("tipoComida")
                ?: TipoComida.DESAYUNO.name
            val tipo = runCatching { TipoComida.valueOf(tipoStr) }
                .getOrDefault(TipoComida.DESAYUNO)
            FoodSearchScreen(
                viewModel = viewModel,
                tipoComida = tipo,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
