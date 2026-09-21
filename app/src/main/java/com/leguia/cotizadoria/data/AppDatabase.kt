package com.leguia.cotizadoria.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ServicioEntity::class, MaterialEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun servicioDao(): ServicioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun obtenerBaseDatos(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cotizador_leguia_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        precargarDatosIniciales(database.servicioDao())
                    }
                }
            }

            suspend fun precargarDatosIniciales(servicioDao: ServicioDao) {
                if (servicioDao.contarServicios() == 0) {
                    val listaServicios = listOf(
                        ServicioEntity(id = 1, nombre = "Carpas", activo = true),
                        ServicioEntity(id = 2, nombre = "Toldos", activo = true),
                        ServicioEntity(id = 3, nombre = "Tapizado", activo = true)
                    )
                    servicioDao.insertarServicios(listaServicios)

                    // Materiales asignados por servicio
                    val listaMateriales = listOf(
                        MaterialEntity(nombre = "Lona Ipalon", servicioId = 1),
                        MaterialEntity(nombre = "Lona Heavy Duty", servicioId = 1),
                        MaterialEntity(nombre = "Malla Raschel", servicioId = 2),
                        MaterialEntity(nombre = "Lona Vinílica", servicioId = 2),
                        MaterialEntity(nombre = "Cuero Sintético", servicioId = 3),
                        MaterialEntity(nombre = "Tela Lino Texturizado", servicioId = 3)
                    )
                    servicioDao.insertarMateriales(listaMateriales)
                }
            }
        }
    }
}