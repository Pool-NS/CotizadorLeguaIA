package com.leguia.cotizadoria.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evento_trazabilidad",
    foreignKeys = [
        ForeignKey(
            entity = CotizacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["cotizacionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("cotizacionId"),
        Index("timestamp")
    ]
)
data class EventoTrazabilidadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cotizacionId: Int?,
    val tipoEvento: String,
    val descripcion: String,
    val timestamp: Long,
    val operador: String
)