package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(servicio: ServicioEntity)

    @Query("SELECT * FROM servicio")
    fun obtenerTodos(): Flow< List< ServicioEntity > >
}