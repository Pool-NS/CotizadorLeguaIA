package com.leguia.cotizadoria.data


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "registro_error",
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
        Index("codigoError")
    ]
)
data class RegistroErrorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cotizacionId: Int,
    val codigoError: String,
    val descripcion: String,
    val atribuibleASistema: Boolean,
    val timestamp: Long
)