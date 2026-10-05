package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CotizacionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarCotizacion(cotizacion: CotizacionEntity): Long

    @Transaction
    suspend fun registrarCotizacionConEvento(cotizacion: CotizacionEntity): Long {
        val id = insertarCotizacion(cotizacion)
        insertarEventoEstado(
            EventoTrazabilidadEntity(
                cotizacionId = id.toInt(),
                tipoEvento = if (cotizacion.estado == "PENDIENTE_PRECIO") "SOLICITUD_REGISTRADA_SIN_PRECIO" else "CREACION_COTIZACION",
                descripcion = if (cotizacion.estado == "PENDIENTE_PRECIO") {
                    "Solicitud ${cotizacion.codigoCotizacion} registrada; falta una regla de precio aprobada."
                } else "Cotización ${cotizacion.codigoCotizacion} registrada.",
                timestamp = System.currentTimeMillis(),
                operador = cotizacion.operador
            )
        )
        return id
    }

    @Update
    suspend fun actualizarCotizacion(cotizacion: CotizacionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarEventoEstado(evento: EventoTrazabilidadEntity)

    @Query("UPDATE cotizacion SET estado = :nuevoEstado, fechaHoraFin = :fechaFin WHERE id = :id AND estado = 'PENDIENTE'")
    suspend fun actualizarEstadoPendiente(id: Int, nuevoEstado: String, fechaFin: Long): Int

    @Transaction
    suspend fun actualizarEstadoConEvento(cotizacion: CotizacionEntity, evento: EventoTrazabilidadEntity) {
        check(actualizarEstadoPendiente(cotizacion.id, cotizacion.estado, cotizacion.fechaHoraFin ?: evento.timestamp) == 1) {
            "La cotización dejó de estar pendiente"
        }
        insertarEventoEstado(evento)
    }

    @Query("SELECT * FROM cotizacion WHERE id = :id")
    suspend fun obtenerPorId(id: Int): CotizacionEntity?

    @Query("SELECT * FROM cotizacion ORDER BY fechaHoraInicio DESC")
    fun obtenerTodas(): Flow< List< CotizacionEntity > >

    @Query("SELECT * FROM cotizacion WHERE estado = :estado ORDER BY fechaHoraInicio DESC")
    fun obtenerPorEstado(estado: String): Flow< List< CotizacionEntity > >
}
