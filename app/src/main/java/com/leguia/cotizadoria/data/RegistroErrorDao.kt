package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroErrorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun registrarError(error: RegistroErrorEntity): Long

    @Query("SELECT * FROM registro_error WHERE cotizacionId = :cotizacionId")
    fun obtenerErroresPorCotizacion(cotizacionId: Int): Flow< List< RegistroErrorEntity > >

    @Query("SELECT * FROM registro_error ORDER BY timestamp DESC")
    fun obtenerTodosLosErrores(): Flow< List< RegistroErrorEntity > >
}