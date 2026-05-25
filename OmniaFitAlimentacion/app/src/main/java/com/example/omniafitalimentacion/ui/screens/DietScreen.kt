package com.example.omniafitalimentacion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.omniafitalimentacion.model.AlimentoItem
import com.example.omniafitalimentacion.model.TipoComida
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

@Composable
fun DietScreen(
    viewModel: DietViewModel,
    onNavigateToMealDetail: (TipoComida) -> Unit
) {
    val alimentos by viewModel.alimentosActivos.collectAsState()

    Scaffold(
        topBar = { OmniaFitTopBar() },
        bottomBar = { OmniaFitBottomNavigation() }
    ) { paddingValues ->
        MealsOverview(
            alimentos = alimentos,
            onMealClick = onNavigateToMealDetail,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        )
    }
}

@Composable
fun MealsOverview(
    alimentos: List<AlimentoItem>,
    onMealClick: (TipoComida) -> Unit,
    modifier: Modifier = Modifier
) {
    val porComida = alimentos.groupBy { it.tipoComida }
    val total = alimentos.sumOf { it.kcal }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(TipoComida.values()) { tipo ->
            val items = porComida[tipo.name].orEmpty()
            MealCard(
                tipo = tipo,
                subtotal = items.sumOf { it.kcal },
                count = items.size,
                onClick = { onMealClick(tipo) }
            )
        }
        item { TotalDiarioCard(totalKcal = total) }
    }
}

@Composable
fun MealCard(
    tipo: TipoComida,
    subtotal: Int,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tipo.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val sufijo = if (count == 1) "alimento" else "alimentos"
                Text(
                    text = "$subtotal Kcal · $count $sufijo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver ${tipo.displayName}"
            )
        }
    }
}

@Composable
fun TotalDiarioCard(totalKcal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB9F6CA))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF1B5E20)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Total del día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$totalKcal Kcal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20)
            )
        }
    }
}

@Composable
fun AlimentoCard(
    alimento: AlimentoItem,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!alimento.imagenUrl.isNullOrBlank()) {
                AsyncImage(
                    model = alimento.imagenUrl,
                    contentDescription = alimento.nombre,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = alimento.nombre, style = MaterialTheme.typography.titleSmall)
                Text(text = "${alimento.kcal} Kcal", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Borrar alimento",
                    tint = MaterialTheme.colorScheme.error
                )
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
