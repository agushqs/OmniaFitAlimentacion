package com.example.omniafitalimentacion.model

import com.google.gson.annotations.SerializedName

// --- Modelo de dominio para la UI (independiente del JSON crudo de OpenFoodFacts) ---
data class Product(
    val foodId: String,
    val productName: String?,
    val description: String?,
    val kcalPor100g: Double,
    val imageUrl: String?
)

// --- JSON crudo devuelto por OpenFoodFacts (cgi/search.pl) ---
data class OffSearchResponse(
    @SerializedName("products") val products: List<OffProduct> = emptyList(),
    @SerializedName("count") val count: Int? = null
)

data class OffProduct(
    @SerializedName("code") val code: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("product_name_es") val productNameEs: String?,
    @SerializedName("brands") val brands: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_small_url") val imageSmallUrl: String?,
    @SerializedName("nutriments") val nutriments: OffNutriments?
)

data class OffNutriments(
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double?,
    @SerializedName("energy-kcal") val energyKcal: Double?
)
