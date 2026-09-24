package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParametroPrecioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarParametros(parametros: List< ParametroPrecioEntity >)

    @Query("SELECT * FROM parametro_precio WHERE servicioId = :servicioId AND activo = 1")
    fun obtenerPorServicio(servicioId: Int): Flow< List< ParametroPrecioEntity > >

    @Query("SELECT * FROM parametro_precio WHERE activo = 1")
    fun obtenerTodosActivos(): Flow< List< ParametroPrecioEntity > >
}