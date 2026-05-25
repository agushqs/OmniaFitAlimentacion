package com.example.omniafitalimentacion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

@Composable
fun FoodSearchScreen(
    viewModel: DietViewModel,
    onNavigateBack: () -> Unit // <-- Nueva función para volver atrás al seleccionar
) {
    var searchText by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Barra de búsqueda
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                if (it.length > 2) viewModel.buscarAlimento(it)
            },
            label = { Text("Buscar alimento (ej: galletas, pollo...)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de resultados
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { producto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // 1. Guardamos el alimento en el ViewModel
                            viewModel.añadirAlimento(producto)
                            // 2. Volvemos a la pantalla anterior
                            onNavigateBack()
                        }
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        // Imagen del alimento cargada por Coil
                        AsyncImage(
                            model = producto.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(producto.productName ?: "Sin nombre", style = MaterialTheme.typography.titleMedium)
                            Text("${producto.nutriments?.energyKcal100g ?: 0} Kcal / 100g", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}