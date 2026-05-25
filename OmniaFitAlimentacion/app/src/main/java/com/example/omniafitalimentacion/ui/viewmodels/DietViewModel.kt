package com.example.omniafitalimentacion.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.omniafitalimentacion.data.network.RetrofitClient
import com.example.omniafitalimentacion.model.Dieta
import com.example.omniafitalimentacion.model.Product
import com.example.omniafitalimentacion.model.AlimentoItem // <-- Asegúrate de tener esto en tu modelo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DietViewModel : ViewModel() {

    private val _dietas = MutableStateFlow<List<Dieta>>(emptyList())
    val dietas: StateFlow<List<Dieta>> = _dietas.asStateFlow()

    // Estado para los resultados de búsqueda
    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    // NUEVO: Estado para almacenar los alimentos que el usuario selecciona de la búsqueda
    private val _alimentosSeleccionados = MutableStateFlow<List<AlimentoItem>>(emptyList())
    val alimentosSeleccionados: StateFlow<List<AlimentoItem>> = _alimentosSeleccionados.asStateFlow()

    // 📍 Función temporal para verificar la API
    fun probarConexionApi() {
        viewModelScope.launch {
            try {
                // Hacemos una búsqueda de prueba: "manzana"
                val response = RetrofitClient.apiService.searchFood("manzana")

                if (response.products.isNotEmpty()) {
                    val primerProducto = response.products[0]
                    Log.d("API_TEST", "✅ ¡Éxito! Producto encontrado: ${primerProducto.productName}")
                    Log.d("API_TEST", "🔥 Calorías (por 100g): ${primerProducto.nutriments?.energyKcal100g}")
                    Log.d("API_TEST", "🖼️ URL de la imagen: ${primerProducto.imageUrl}")
                } else {
                    Log.w("API_TEST", "⚠️ La API respondió correctamente, pero la lista de productos está vacía.")
                }
            } catch (e: Exception) {
                // Si falta el permiso de internet o hay un error de red, caerá aquí
                Log.e("API_TEST", "❌ Error en la conexión: ${e.message}")
            }
        }
    }

    fun crearNuevaDieta() {
        val nuevaDieta = listOf(Dieta(1, "Hoy"))
        _dietas.value = nuevaDieta
    }

    fun buscarAlimento(query: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.searchFood(query)
                _searchResults.value = response.products
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error buscando: ${e.message}")
            }
        }
    }

    // NUEVO: Función que se ejecuta al pulsar un alimento en la pantalla de búsqueda
    fun añadirAlimento(producto: Product) {
        val nuevoAlimento = AlimentoItem(
            nombre = producto.productName ?: "Desconocido",
            kcal = producto.nutriments?.energyKcal100g?.toInt() ?: 0,
            imagenUrl = producto.imageUrl
        )
        // Añadimos el nuevo alimento conservando los que ya estaban en la lista
        _alimentosSeleccionados.value = _alimentosSeleccionados.value + nuevoAlimento
    }
}

