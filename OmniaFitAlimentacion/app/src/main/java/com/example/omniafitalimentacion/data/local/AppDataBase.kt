package com.example.omniafitalimentacion.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.omniafitalimentacion.model.AlimentoItem
import com.example.omniafitalimentacion.model.Dieta

// @Database le dice a Room qué tablas (entities) viven aquí y en qué versión está
// el schema. Cada vez que cambiamos una tabla (p.ej. añadimos una columna) tenemos
// que subir el número de version, si no Room crashea al detectar inconsistencias.
@Database(entities = [Dieta::class, AlimentoItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Room implementa este método solo: no escribimos código, solo declaramos que
    // queremos un DietDao. La librería genera la implementación en tiempo de compilación.
    abstract fun dietDao(): DietDao

    companion object {
        // @Volatile garantiza que todos los hilos vean siempre el último valor de
        // INSTANCE escrito por cualquier otro hilo (sin esto un hilo podría tener
        // una copia en caché desactualizada en su CPU).
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Patrón "double-checked locking":
        //  - Si INSTANCE ya está creada, devolvemos directamente sin entrar en el
        //    synchronized (que es caro porque bloquea otros hilos).
        //  - Si no, entramos al bloque sincronizado y la creamos. Solo el primer
        //    hilo que llegue construirá la BD; los demás esperan y la reutilizan.
        // Resultado: una única instancia de la BD compartida por toda la app, sin
        // pagar el coste del lock en cada llamada.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omniafit_database"
                )
                    // Cuando subimos la versión del @Database, le decimos a Room:
                    // "si el schema cambió y no hay migración escrita, borra todo
                    // y empieza de cero". Es la opción cómoda en desarrollo; en
                    // una app de verdad escribiríamos Migration manuales para no
                    // perder los datos del usuario.
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
