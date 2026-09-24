package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CotizacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCotizacion(cotizacion: CotizacionEntity): Long

    @Update
    suspend fun actualizarCotizacion(cotizacion: CotizacionEntity)

    @Query("SELECT * FROM cotizacion WHERE id = :id")
    suspend fun obtenerPorId(id: Int): CotizacionEntity?

    @Query("SELECT * FROM cotizacion ORDER BY fechaHoraInicio DESC")
    fun obtenerTodas(): Flow< List< CotizacionEntity > >

    @Query("SELECT * FROM cotizacion WHERE estado = :estado ORDER BY fechaHoraInicio DESC")
    fun obtenerPorEstado(estado: String): Flow< List< CotizacionEntity > >
}