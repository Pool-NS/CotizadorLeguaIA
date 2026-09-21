package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicioDao {

    @Query("SELECT * FROM servicio WHERE activo = 1")
    fun obtenerServiciosActivos(): Flow<List<ServicioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarServicios(servicios: List<ServicioEntity>)

    @Query("SELECT COUNT(*) FROM servicio")
    suspend fun contarServicios(): Int

    // --- Nuevas consultas para Materiales ---
    @Query("SELECT * FROM material WHERE servicioId = :servicioId AND activo = 1")
    fun obtenerMaterialesPorServicio(servicioId: Int): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarMateriales(materiales: List<MaterialEntity>)

    @Query("SELECT COUNT(*) FROM material")
    suspend fun contarMateriales(): Int
}