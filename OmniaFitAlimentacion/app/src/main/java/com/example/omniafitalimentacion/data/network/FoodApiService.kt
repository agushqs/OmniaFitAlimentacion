package com.example.omniafitalimentacion.data.network

import com.example.omniafitalimentacion.model.FoodSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApiService {

    // Este es el endpoint de búsqueda de la API
    @GET("cgi/search.pl?search_simple=1&action=process&json=1")
    suspend fun searchFood(
        // Aquí pasaremos el texto que escriba el usuario (ej: "pollo")
        @Query("search_terms") query: String
    ): FoodSearchResponse

}