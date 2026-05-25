package com.example.omniafitalimentacion.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.omniafitalimentacion.model.AlimentoItem
import com.example.omniafitalimentacion.model.Dieta

// Declaramos las tablas (entities) y la versión de la base de datos
@Database(entities = [Dieta::class, AlimentoItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dietDao(): DietDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Función para obtener la base de datos asegurando que solo haya 1 instancia abierta
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omniafit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}