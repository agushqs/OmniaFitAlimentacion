package com.example.omniafitalimentacion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.omniafitalimentacion.model.Product
import com.example.omniafitalimentacion.model.TipoComida
import com.example.omniafitalimentacion.ui.viewmodels.DietViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    viewModel: DietViewModel,
    tipoComida: TipoComida,
    onNavigateBack: () -> Unit
) {
    // En Compose, "remember + mutableStateOf" = variable que sobrevive a recomposiciones
    // y dispara una nueva recomposición cuando cambia. Sin remember, cada vez que Compose
    // redibujara la pantalla, searchText volvería a "" y perderíamos lo que el usuario
    // estaba escribiendo.
    var searchText by remember { mutableStateOf("") }

    // Estos vienen del ViewModel y reflejan el estado global de la búsqueda. collectAsState
    // suscribe esta pantalla al StateFlow y la recompone cuando cambian.
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // Estado del diálogo "¿Cuántos gramos?": qué producto eligió el usuario y si el
    // diálogo está abierto.
    var productoSeleccionado by remember { mutableStateOf<Product?>(null) }
    var mostrarDialogoGramos by remember { mutableStateOf(false) }
    var gramosInput by remember { mutableStateOf("") }

    // Estado del diálogo "Alimento personalizado". El contenido de sus campos se
    // gestiona DENTRO del propio diálogo (ver más abajo).
    var mostrarDialogoCustom by remember { mutableStateOf(false) }

    // Acceso al teclado virtual y al foco. Los usamos para cerrar el teclado tras
    // pulsar la lupa o la tecla "Buscar" del propio teclado: pequeño detalle de UX
    // que evita que el teclado tape los resultados.
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Definimos la acción una sola vez para reutilizarla desde dos sitios (botón lupa
    // y tecla Search del teclado). Mantiene la lógica en un solo lugar.
    val lanzarBusqueda: () -> Unit = {
        if (searchText.isNotBlank()) {
            viewModel.buscarAlimento(searchText)
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir a ${tipoComida.displayName}") },
                // navigationIcon = el botón a la izquierda del título (convención
                // Android para "volver"). Lo usamos como "cancelar".
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancelar y volver"
                        )
                    }
                },
                // actions = lo que va a la derecha. Doblamos el cancelar como botón
                // explícito por si el usuario no entiende la flecha.
                actions = {
                    TextButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB9F6CA))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Barra de búsqueda + botón lupa, alineados en horizontal.
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        // Búsqueda automática conforme se escribe. Esperamos a tener
                        // más de 2 letras para no inundar la API con queries de
                        // una sola letra que devolverían miles de productos.
                        if (it.length > 2) viewModel.buscarAlimento(it)
                    },
                    label = { Text("Buscar alimento (ej: arroz, manzana...)") },
                    singleLine = true,
                    // imeAction = Search hace que el teclado muestre una lupa en
                    // lugar del Enter normal.
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    // Y aquí enganchamos qué pasa al pulsar esa lupa.
                    keyboardActions = KeyboardActions(onSearch = { lanzarBusqueda() }),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = lanzarBusqueda,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para añadir un alimento personalizado (sin pasar por la API).
            // Útil cuando OFF no tiene el producto que el usuario quiere.
            OutlinedButton(
                onClick = { mostrarDialogoCustom = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir alimento personalizado")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tres estados visuales mutuamente excluyentes:
            //  - Buscando → spinner
            //  - Hay texto pero sin resultados → mensaje sugiriendo el botón custom
            //  - Hay resultados → la LazyColumn de abajo los pinta
            if (isSearching) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buscando...", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (results.isEmpty() && searchText.isNotBlank()) {
                Text(
                    text = "Sin resultados. Prueba con otra palabra o añade uno personalizado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // LazyColumn = lista que solo crea las tarjetas visibles en pantalla
            // (como un RecyclerView). Crucial para listas grandes.
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { producto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Guardamos qué producto pulsó y abrimos el diálogo
                                // de gramos. Lo añadiremos cuando confirme.
                                productoSeleccionado = producto
                                mostrarDialogoGramos = true
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = producto.productName ?: "Sin nombre",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${"%.0f".format(producto.kcalPor100g)} Kcal / 100g",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!producto.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = producto.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Diálogo "¿Cuántos gramos?" para alimentos venidos de la API ---
    if (mostrarDialogoGramos) {
        AlertDialog(
            // Se llama al pulsar fuera o al pulsar atrás. Equivalente a Cancelar.
            onDismissRequest = { mostrarDialogoGramos = false },
            title = { Text("¿Cuántos gramos?") },
            text = {
                OutlinedTextField(
                    value = gramosInput,
                    onValueChange = { gramosInput = it },
                    label = { Text("Gramos (ej: 150)") },
                    singleLine = true,
                    // Forzamos teclado numérico: más cómodo para escribir cifras.
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // toDoubleOrNull es nuestro amigo: si el usuario escribió
                        // letras u otra basura, devuelve null y caemos a 0.0 sin
                        // crashear.
                        val gramos = gramosInput.toDoubleOrNull() ?: 0.0
                        productoSeleccionado?.let { producto ->
                            viewModel.añadirAlimentoADieta(producto, gramos, tipoComida)
                        }
                        mostrarDialogoGramos = false
                        gramosInput = ""
                        // Tras añadir, volvemos a la pantalla anterior (detalle de
                        // la comida) para que el usuario vea su alimento ya en la lista.
                        onNavigateBack()
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoGramos = false
                    gramosInput = "" // limpiar también al cancelar
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- Diálogo "Alimento personalizado" (nombre + calorías totales) ---
    if (mostrarDialogoCustom) {
        // OJO: estos `remember` están DENTRO del if. Eso es a propósito: cuando el
        // diálogo se cierra, Compose destruye este bloque y la próxima vez que se
        // abra empieza con los campos vacíos. Si los pusiéramos fuera del if, el
        // nombre y las kcal del intento anterior seguirían ahí. Es un patrón típico
        // de "estado local de un diálogo".
        var nombreCustom by remember { mutableStateOf("") }
        var kcalCustom by remember { mutableStateOf("") }
        val kcalParsed = kcalCustom.toIntOrNull()
        // Validación: necesitamos nombre no vacío y un número de kcal >= 0. Solo si
        // ambas condiciones se cumplen activamos el botón "Añadir".
        val datosValidos = nombreCustom.isNotBlank() && kcalParsed != null && kcalParsed >= 0

        AlertDialog(
            onDismissRequest = { mostrarDialogoCustom = false },
            title = { Text("Alimento personalizado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreCustom,
                        onValueChange = { nombreCustom = it },
                        label = { Text("Nombre (ej: Tortilla casera)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = kcalCustom,
                        // Filtramos a solo dígitos: aunque pongamos teclado numérico,
                        // en algunos móviles también se pueden meter "." o "-". Así
                        // garantizamos que el toIntOrNull no vea basura.
                        onValueChange = { input -> kcalCustom = input.filter { it.isDigit() } },
                        label = { Text("Calorías totales (Kcal)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = datosValidos, // botón gris si falta algo
                    onClick = {
                        viewModel.añadirAlimentoCustom(
                            nombre = nombreCustom,
                            kcal = kcalParsed ?: 0,
                            tipoComida = tipoComida
                        )
                        mostrarDialogoCustom = false
                        onNavigateBack()
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCustom = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
