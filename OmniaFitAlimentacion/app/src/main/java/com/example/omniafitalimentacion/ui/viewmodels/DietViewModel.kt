package com.example.omniafitalimentacion.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.omniafitalimentacion.data.local.DietDao
import com.example.omniafitalimentacion.data.network.RetrofitClient
import com.example.omniafitalimentacion.model.AlimentoItem
import com.example.omniafitalimentacion.model.Dieta
import com.example.omniafitalimentacion.model.OffProduct
import com.example.omniafitalimentacion.model.Product
import com.example.omniafitalimentacion.model.TipoComida
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// El ViewModel es la "memoria viva" de la pantalla: sobrevive a rotaciones del móvil
// y guarda el estado que la UI necesita pintar. La UI nunca toca la BD directamente,
// le pregunta al ViewModel y este se entiende con el DAO.
class DietViewModel(private val dao: DietDao) : ViewModel() {

    // dao.obtenerTodasLasDietas() devuelve un Flow "frío" (solo emite cuando alguien
    // lo recolecta). stateIn lo convierte en un StateFlow "caliente":
    //  - Comparte una sola conexión con la BD entre todos los suscriptores
    //  - Recuerda el último valor emitido (útil si una pantalla se suscribe tarde)
    //  - WhileSubscribed(5000): se mantiene activo 5s tras quedarse sin suscriptores,
    //    para no resetearse en cada rotación o cambio de pantalla.
    val dietas: StateFlow<List<Dieta>> = dao.obtenerTodasLasDietas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Patrón habitual en MVVM con Compose: una versión privada mutable (_xxx) que solo
    // el ViewModel puede cambiar, y una pública inmutable que la UI observa. Así la
    // UI no puede modificar el estado por accidente.
    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _dietaActivaId = MutableStateFlow<Int?>(null)
    val dietaActivaId: StateFlow<Int?> = _dietaActivaId.asStateFlow()

    private val _alimentosActivos = MutableStateFlow<List<AlimentoItem>>(emptyList())
    val alimentosActivos: StateFlow<List<AlimentoItem>> = _alimentosActivos.asStateFlow()

    // null = todavía estamos consultando la BD. Tras el init pasa a "welcome_screen"
    // (si no hay alimentos) o "diet_screen" (si ya hay alguno guardado).
    private val _rutaInicial = MutableStateFlow<String?>(null)
    val rutaInicial: StateFlow<String?> = _rutaInicial.asStateFlow()

    init {
        // El bloque init se ejecuta al construir el ViewModel. Aquí lanzamos dos
        // corrutinas independientes (viewModelScope.launch) que viven mientras viva
        // el ViewModel y se cancelan solas cuando la pantalla muere.

        // 1) Consulta única para decidir la pantalla inicial. Si ya hay algún alimento
        //    guardado de sesiones anteriores, saltamos welcome para que no parpadee.
        viewModelScope.launch {
            val hayAlimentos = dao.contarAlimentos() > 0
            _rutaInicial.value = if (hayAlimentos) "diet_screen" else "welcome_screen"
        }

        // 2) Observamos la lista de dietas en tiempo real. Cuando aparece la primera
        //    (creada por el usuario en welcome) la marcamos como activa para que las
        //    demás pantallas sepan a qué dieta apuntan los alimentos que añadan.
        viewModelScope.launch {
            dietas.collect { lista ->
                if (lista.isNotEmpty() && _dietaActivaId.value == null) {
                    seleccionarDieta(lista.first().id)
                }
            }
        }
    }

    fun seleccionarDieta(id: Int) {
        _dietaActivaId.value = id
        // Cada vez que cambiamos de dieta arrancamos una nueva corrutina que escucha
        // sus alimentos. Mientras esa corrutina viva, _alimentosActivos se irá
        // actualizando solo al insertar/borrar en la BD (Room avisa por el Flow).
        viewModelScope.launch {
            dao.obtenerAlimentosPorDieta(id).collect { lista ->
                _alimentosActivos.value = lista
            }
        }
    }

    // Idempotente: si ya existe alguna dieta no crea otra, solo se asegura de que
    // haya una activa. Así el botón "+" del welcome se puede pulsar varias veces
    // (o por dos usuarios pulsando rapidísimo) sin acabar con dietas duplicadas.
    fun crearNuevaDieta(nombre: String, fecha: String) {
        viewModelScope.launch {
            // .first() coge la primera emisión del Flow y termina; equivale a leer la
            // foto actual de la tabla sin quedarnos escuchando para siempre.
            val actuales = dao.obtenerTodasLasDietas().first()
            if (actuales.isEmpty()) {
                val nuevaDieta = Dieta(nombre = nombre, fechaAsignada = fecha)
                val nuevoId = dao.insertarDieta(nuevaDieta)
                seleccionarDieta(nuevoId.toInt())
            } else if (_dietaActivaId.value == null) {
                seleccionarDieta(actuales.first().id)
            }
        }
    }

    fun buscarAlimento(query: String) {
        val q = query.trim()
        if (q.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val response = RetrofitClient.apiService.searchFood(q)
                val productos = response.products
                    .map { it.toProduct() }
                    // OpenFoodFacts a veces devuelve productos sin nombre (datos
                    // incompletos en su BD colaborativa). Los filtramos para que
                    // no aparezcan tarjetas vacías.
                    .filter { !it.productName.isNullOrBlank() }
                _searchResults.value = productos
            } catch (e: Exception) {
                // Capturamos cualquier error de red para que la app no se cierre.
                // El usuario verá la lista vacía y el log nos cuenta qué pasó.
                Log.e("API_ERROR", "Error buscando: ${e.message}", e)
                _searchResults.value = emptyList()
            } finally {
                // try/finally para apagar el spinner haya ido bien o mal la llamada.
                _isSearching.value = false
            }
        }
    }

    fun añadirAlimentoADieta(producto: Product, gramos: Double, tipoComida: TipoComida) {
        // El ?: return es una salida temprana: si no hay dieta activa no hacemos nada
        // (en la práctica no debería pasar, pero curarse en salud no cuesta).
        val idDietaActual = _dietaActivaId.value ?: return

        // Regla de tres: la API nos da kcal por 100 g, el usuario ha dicho cuántos g
        // se ha comido. Convertimos a kcal totales de SU ración.
        val kcalCalculadas = (producto.kcalPor100g / 100.0) * gramos

        val nuevoAlimento = AlimentoItem(
            dietaId = idDietaActual,
            tipoComida = tipoComida.name,
            nombre = producto.productName ?: "Alimento genérico",
            kcal = kcalCalculadas.toInt(),
            imagenUrl = producto.imageUrl
        )

        viewModelScope.launch {
            dao.insertarAlimento(nuevoAlimento)
        }
    }

    fun eliminarAlimentoDeDieta(alimento: AlimentoItem) {
        viewModelScope.launch {
            dao.eliminarAlimento(alimento)
        }
    }

    // Alimento creado a mano por el usuario: ya viene con sus kcal totales,
    // no hace falta regla de tres con gramos.
    fun añadirAlimentoCustom(nombre: String, kcal: Int, tipoComida: TipoComida) {
        val idDietaActual = _dietaActivaId.value ?: return
        val nuevoAlimento = AlimentoItem(
            dietaId = idDietaActual,
            tipoComida = tipoComida.name,
            // ifBlank actúa como red de seguridad: si el usuario deja el nombre vacío
            // ponemos un texto genérico para no guardar un alimento "sin nombre".
            nombre = nombre.trim().ifBlank { "Alimento personalizado" },
            // coerceAtLeast(0) evita kcal negativas si por algún bug llegan.
            kcal = kcal.coerceAtLeast(0),
            imagenUrl = null
        )
        viewModelScope.launch {
            dao.insertarAlimento(nuevoAlimento)
        }
    }

    // --- Conversión de OpenFoodFacts a nuestro modelo Product ---
    // OffProduct viene del JSON tal cual; Product es lo que la UI consume. Mantener
    // ambos separados nos deja cambiar de API sin tocar la UI (como hicimos cuando
    // probamos FatSecret).
    private fun OffProduct.toProduct(): Product {
        // OFF a veces trae el nombre solo en inglés y a veces tiene un campo en
        // español (product_name_es). Preferimos el español si existe.
        val nombre = productNameEs?.takeIf { it.isNotBlank() }
            ?: productName?.takeIf { it.isNotBlank() }
        // energy-kcal_100g es lo normal; energy-kcal es un fallback para productos
        // que no están normalizados a 100 g en la BD de OFF.
        val kcal = nutriments?.energyKcal100g ?: nutriments?.energyKcal ?: 0.0
        return Product(
            foodId = code ?: nombre.orEmpty(),
            productName = nombre,
            description = brands?.takeIf { it.isNotBlank() },
            kcalPor100g = kcal,
            // Para la miniatura preferimos image_small_url (más rápido de descargar);
            // si no existe, caemos a image_url (la imagen grande).
            imageUrl = imageSmallUrl?.takeIf { it.isNotBlank() } ?: imageUrl
        )
    }
}

// Necesitamos una Factory porque DietViewModel recibe un parámetro (el DAO) en su
// constructor, y el sistema de ViewModels de Android por defecto solo sabe crear
// ViewModels SIN argumentos. La Factory le enseña a fabricar el nuestro.
class DietViewModelFactory(private val dao: DietDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DietViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
