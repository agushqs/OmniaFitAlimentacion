package com.example.omniafitalimentacion.model

import com.google.gson.annotations.SerializedName

// Esta es la respuesta principal que engloba toda la lista de productos
data class FoodSearchResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("products") val products: List<Product>
)

// Cada producto individual (un alimento)
data class Product(
    @SerializedName("_id") val id: String,
    // El nombre del producto en español
    @SerializedName("product_name_es") val productName: String?,
    // La URL de la foto que mostraremos en la pantalla
    @SerializedName("image_url") val imageUrl: String?,
    // Los datos nutricionales
    @SerializedName("nutriments") val nutriments: Nutriments?
)

// Desglosamos los nutrientes para sacar las calorías por 100g
data class Nutriments(
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double?
)