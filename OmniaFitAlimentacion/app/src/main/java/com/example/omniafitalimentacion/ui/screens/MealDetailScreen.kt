package com.example.omniafitalimentacion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.omniafitalimentacion.model.TipoComida
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailScreen(
    viewModel: DietViewModel,
    tipoComida: TipoComida,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val alimentos by viewModel.alimentosActivos.collectAsState()
    val alimentosDeComida = alimentos.filter { it.tipoComida == tipoComida.name }
    val subtotal = alimentosDeComida.sumOf { it.kcal }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tipoComida.displayName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB9F6CA))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToSearch,
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                text = { Text("Añadir") },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "$subtotal Kcal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            val sufijo = if (alimentosDeComida.size == 1) "alimento" else "alimentos"
            Text(
                text = "${alimentosDeComida.size} $sufijo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (alimentosDeComida.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin alimentos en ${tipoComida.displayName.lowercase()}.\nPulsa Añadir para empezar.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(alimentosDeComida) { alimento ->
                        AlimentoCard(
                            alimento = alimento,
                            onDeleteClick = { viewModel.eliminarAlimentoDeDieta(alimento) }
                        )
                    }
                }
            }
        }
    }
}
