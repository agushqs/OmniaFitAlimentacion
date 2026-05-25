package com.example.omniafitalimentacion.model

import com.google.gson.annotations.SerializedName

data class FoodSearchResponse(
    val products: List<Product>
)

data class Product(
    @SerializedName("product_name")
    val productName: String?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("nutriments")
    val nutriments: Nutriments?
)

data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Double?
)