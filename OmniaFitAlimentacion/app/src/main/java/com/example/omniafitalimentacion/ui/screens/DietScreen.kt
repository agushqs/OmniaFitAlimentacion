package com.example.omniafitalimentacion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.omniafitalimentacion.model.AlimentoItem
import com.example.omniafitalimentacion.model.Dieta
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

@Composable
fun DietScreen(
    viewModel: DietViewModel,
    onNavigateToSearch: () -> Unit
) {
    val dietas by viewModel.dietas.collectAsState()
    // NUEVO: Observamos los alimentos que el usuario ha ido añadiendo
    val alimentos by viewModel.alimentosSeleccionados.collectAsState()

    Scaffold(
        topBar = { OmniaFitTopBar() },
        bottomBar = { OmniaFitBottomNavigation() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (dietas.isEmpty()) {
                // Si no hay dietas, el botón de añadir crea una dieta vacía para empezar
                EmptyDietState(onAddClick = { viewModel.crearNuevaDieta() })
            } else {
                // Pasamos la lista de alimentos a la pantalla activa
                ActiveDietState(
                    alimentos = alimentos,
                    onAddFoodClick = onNavigateToSearch
                )
            }
        }
    }
}

@Composable
fun EmptyDietState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AÑADIR DIETAS", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Añadir dieta",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun ActiveDietState(alimentos: List<AlimentoItem>, onAddFoodClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Calendario (Placeholder)
        Card(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("📅 Calendario Activo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón general para ir al buscador (simulando que añades al desayuno/almuerzo por ahora)
        Button(
            onClick = onAddFoodClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("BUSCAR Y AÑADIR ALIMENTO")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NUEVO: Título dinámico que suma las calorías totales
        val totalKcal = alimentos.sumOf { it.kcal }
        Text("Alimentos Consumidos ($totalKcal Kcal)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // NUEVO: Lista dinámica de los alimentos que has seleccionado
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (alimentos.isEmpty()) {
                item {
                    Text("Aún no has añadido alimentos hoy.", color = Color.Gray)
                }
            } else {
                items(alimentos) { alimento ->
                    AlimentoCard(alimento)
                }
            }
        }
    }
}

// NUEVO: Tarjeta diseñada para mostrar los alimentos añadidos
@Composable
fun AlimentoCard(alimento: AlimentoItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = alimento.imagenUrl,
                contentDescription = alimento.nombre,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = alimento.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = "${alimento.kcal} Kcal", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniaFitTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.width(8.dp))
                Text("OmniaFit")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB9F6CA))
    )
}

@Composable
fun OmniaFitBottomNavigation() {
    NavigationBar(containerColor = Color(0xFFB9F6CA)) {
        NavigationBarItem(icon = { Icon(Icons.Default.Home, "") }, selected = false, onClick = {})
        NavigationBarItem(icon = { Icon(Icons.Default.DateRange, "") }, selected = true, onClick = {})
        NavigationBarItem(icon = { Icon(Icons.Default.Settings, "") }, selected = false, onClick = {})
        NavigationBarItem(icon = { Icon(Icons.Default.Build, "") }, selected = false, onClick = {})
    }
}