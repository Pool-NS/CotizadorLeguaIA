package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ParametroPrecioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarParametros(parametros: List< ParametroPrecioEntity >)

    @Query("SELECT * FROM parametro_precio WHERE servicioId = :servicioId AND activo = 1")
    fun obtenerPorServicio(servicioId: Int): Flow< List< ParametroPrecioEntity > >

    @Query("SELECT * FROM parametro_precio WHERE activo = 1")
    fun obtenerTodosActivos(): Flow< List< ParametroPrecioEntity > >

    @Query("UPDATE parametro_precio SET activo = 0 WHERE servicioId = :servicioId AND concepto = :concepto")
    suspend fun desactivarTarifaExacta(servicioId: Int, concepto: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(parametro: ParametroPrecioEntity)

    @Transaction
    suspend fun guardarTarifaAprobada(servicioId: Int, medida: String, precio: Double) {
        require(precio.isFinite() && precio > 0.0 && medida.startsWith("MEDIDA:"))
        desactivarTarifaExacta(servicioId, medida)
        insertar(ParametroPrecioEntity(servicioId = servicioId, concepto = medida, precioUnitario = precio, unidadMedida = "cotización", activo = true))
    }
}
