package com.example.omniafitalimentacion.model

data class Dieta(
    val id: Int,
    val fecha: String
)

data class AlimentoItem(
    val nombre: String,
    val kcal: Int,
    val imagenUrl: String?
)