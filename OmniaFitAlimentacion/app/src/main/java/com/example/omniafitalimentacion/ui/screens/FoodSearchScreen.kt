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
import com.example.omniafitalimentacion.model.Product // <-- Importante: Importa tu modelo Product
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

@Composable
fun FoodSearchScreen(
    viewModel: DietViewModel,
    onNavigateBack: () -> Unit // <-- Nueva función para volver atrás al seleccionar
) {
    var searchText by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()

    // --- NUEVAS VARIABLES PARA EL DIÁLOGO DE GRAMOS ---
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var gramosInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Barra de búsqueda
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                if (it.length > 2) viewModel.buscarAlimento(it)
            },
            label = { Text("Buscar alimento (ej: arroz, manzana...)") },
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
                            // En lugar de guardar directo, guardamos cuál ha tocado y abrimos el diálogo
                            productoSeleccionado = producto
                            mostrarDialogo = true
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

    // --- VENTANA EMERGENTE (POP-UP) PARA PEDIR LOS GRAMOS ---
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("¿Cuántos gramos?") },
            text = {
                OutlinedTextField(
                    value = gramosInput,
                    onValueChange = { gramosInput = it },
                    label = { Text("Gramos (ej: 150)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Convertimos lo que escribió a número (si escribe letras, ponemos 0.0)
                        val gramos = gramosInput.toDoubleOrNull() ?: 0.0

                        productoSeleccionado?.let { producto ->
                            // 1. Enviamos el producto Y los gramos al ViewModel para que haga el cálculo
                            viewModel.añadirAlimentoADieta(producto, gramos)
                        }

                        // 2. Cerramos el diálogo, limpiamos el texto y volvemos a la pantalla principal
                        mostrarDialogo = false
                        gramosInput = ""
                        onNavigateBack()
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}