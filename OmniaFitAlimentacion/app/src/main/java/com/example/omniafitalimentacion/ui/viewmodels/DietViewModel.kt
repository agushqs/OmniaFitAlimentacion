package com.example.omniafitalimentacion.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.omniafitalimentacion.data.local.DietDao
import com.example.omniafitalimentacion.data.network.RetrofitClient
import com.example.omniafitalimentacion.model.AlimentoItem
import com.example.omniafitalimentacion.model.Dieta
import com.example.omniafitalimentacion.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.room.Dao

class DietViewModel(private val dao: DietDao) : ViewModel() {

    val dietas: StateFlow<List<Dieta>> = dao.obtenerTodasLasDietas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    private val _dietaActivaId = MutableStateFlow<Int?>(null)
    val dietaActivaId: StateFlow<Int?> = _dietaActivaId.asStateFlow()

    private val _alimentosActivos = MutableStateFlow<List<AlimentoItem>>(emptyList())
    val alimentosActivos: StateFlow<List<AlimentoItem>> = _alimentosActivos.asStateFlow()

    init {
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
        viewModelScope.launch {
            dao.obtenerAlimentosPorDieta(id).collect { lista ->
                _alimentosActivos.value = lista
            }
        }
    }

    fun crearNuevaDieta(nombre: String, fecha: String) {
        viewModelScope.launch {
            val nuevaDieta = Dieta(nombre = nombre, fechaAsignada = fecha)
            val nuevoId = dao.insertarDieta(nuevaDieta)
            seleccionarDieta(nuevoId.toInt())
        }
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

    // --- LA NUEVA FUNCIÓN QUE CALCULA LAS CALORÍAS SEGÚN LOS GRAMOS ---
    fun añadirAlimentoADieta(producto: Product, gramos: Double) {
        val idDietaActual = _dietaActivaId.value ?: return

        // Regla de tres: (Kcal por 100g / 100) * gramos del usuario
        val kcalPor100g = producto.nutriments?.energyKcal100g ?: 0.0
        val kcalCalculadas = (kcalPor100g / 100.0) * gramos

        val nuevoAlimento = AlimentoItem(
            dietaId = idDietaActual,
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
}

class DietViewModelFactory(private val dao: DietDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DietViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

