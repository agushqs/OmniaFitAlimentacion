package com.example.omniafitalimentacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.omniafitalimentacion.ui.AppNavigation
import com.example.omniafitalimentacion.ui.theme.OmniaFitAlimentacionTheme
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

class MainActivity : ComponentActivity() {

    // Instanciamos el ViewModel que guardará los datos de las dietas y el buscador
    private val dietViewModel: DietViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Asegúrate de que este nombre coincide con el tema de tu app
            OmniaFitAlimentacionTheme {
                // Arrancamos el sistema de navegación
                AppNavigation(viewModel = dietViewModel)
            }
        }
    }
}