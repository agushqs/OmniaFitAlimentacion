package com.example.omniafitalimentacion.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Las 4 comidas del día. Se persiste como String (TipoComida.name) en la BD.
enum class TipoComida(val displayName: String) {
    DESAYUNO("Desayuno"),
    ALMUERZO("Almuerzo"),
    MERIENDA("Merienda"),
    CENA("Cena")
}

// @Entity le dice a Android: "Crea una tabla en el móvil para guardar esto"
@Entity(tableName = "tabla_dietas")
data class Dieta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // El ID se genera solo (1, 2, 3...)

    val nombre: String, // ej: "Dieta Volumen", "Dieta Lunes"
    val fechaAsignada: String // Aquí guardaremos el día (ej: "2024-05-27")
)

@Entity(tableName = "tabla_alimentos")
data class AlimentoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val dietaId: Int, // Para saber a qué dieta pertenece este alimento
    val tipoComida: String, // TipoComida.name: "DESAYUNO" / "ALMUERZO" / "MERIENDA" / "CENA"
    val nombre: String,
    val kcal: Int,
    val imagenUrl: String?
)
