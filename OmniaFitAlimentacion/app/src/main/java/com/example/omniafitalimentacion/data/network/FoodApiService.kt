package com.example.omniafitalimentacion.data.network

import com.example.omniafitalimentacion.model.OffSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FoodApiService {
    @GET("cgi/search.pl?search_simple=1&action=process&json=1&fields=code,product_name,product_name_es,brands,image_url,image_small_url,nutriments&lc=es")
    suspend fun searchFood(
        @Query("search_terms") query: String,
        @Query("page_size") pageSize: Int = 20
    ): OffSearchResponse
}
