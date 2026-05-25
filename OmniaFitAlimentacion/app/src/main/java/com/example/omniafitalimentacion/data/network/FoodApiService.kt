package com.example.omniafitalimentacion.data.network

import com.example.omniafitalimentacion.model.FoodSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApiService {
    @GET("cgi/search.pl?search_simple=1&action=process&json=1&fields=product_name,nutriments,image_url")
    suspend fun searchFood(
        @Query("search_terms") query: String
    ): FoodSearchResponse
}