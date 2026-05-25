package com.example.omniafitalimentacion.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.omniafitalimentacion.model.AlimentoItem
import com.example.omniafitalimentacion.model.Dieta
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {

    // --- OPERACIONES PARA DIETAS ---

    // Inserta una dieta y devuelve el ID que se le ha generado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDieta(dieta: Dieta): Long

    // Lee todas las dietas en tiempo real (Flow avisa automáticamente a la UI si hay cambios)
    @Query("SELECT * FROM tabla_dietas")
    fun obtenerTodasLasDietas(): Flow<List<Dieta>>


    // --- OPERACIONES PARA ALIMENTOS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlimento(alimento: AlimentoItem)

    // Busca solo los alimentos que pertenecen a una dieta específica
    @Query("SELECT * FROM tabla_alimentos WHERE dietaId = :dietaId")
    fun obtenerAlimentosPorDieta(dietaId: Int): Flow<List<AlimentoItem>>

    @Delete
    suspend fun eliminarAlimento(alimento: AlimentoItem)

    // Cuenta total de alimentos registrados (en cualquier dieta y comida).
    @Query("SELECT COUNT(*) FROM tabla_alimentos")
    suspend fun contarAlimentos(): Int
}