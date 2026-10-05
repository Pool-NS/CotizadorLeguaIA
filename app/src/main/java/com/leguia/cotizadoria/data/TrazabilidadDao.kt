package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrazabilidadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvento(evento: EventoTrazabilidadEntity): Long

    @Query("SELECT * FROM evento_trazabilidad ORDER BY timestamp DESC")
    fun obtenerTodosLosEventos(): Flow< List< EventoTrazabilidadEntity > >

    @Query("SELECT * FROM evento_trazabilidad WHERE cotizacionId = :cotizacionId ORDER BY timestamp ASC")
    fun obtenerEventosPorCotizacion(cotizacionId: Int): Flow< List< EventoTrazabilidadEntity > >
}