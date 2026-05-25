package com.example.omniafitalimentacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.omniafitalimentacion.data.local.AppDatabase
import com.example.omniafitalimentacion.ui.AppNavigation
import com.example.omniafitalimentacion.ui.theme.OmniaFitAlimentacionTheme // <-- Pon el tuyo
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializamos la base de datos de Room
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.dietDao()

        setContent {
            // 2. Creamos el ViewModel usando nuestra nueva Fábrica
            val dietViewModel: DietViewModel = viewModel(
                factory = DietViewModelFactory(dao)
            )

            OmniaFitAlimentacionTheme {
                // 3. Arrancamos la navegación como siempre
                AppNavigation(viewModel = dietViewModel)
            }
        }
    }
}