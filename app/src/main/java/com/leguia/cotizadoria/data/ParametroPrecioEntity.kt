package com.leguia.cotizadoria.data


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parametro_precio",
    foreignKeys = [
        ForeignKey(
            entity = ServicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["servicioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("servicioId")]
)
data class ParametroPrecioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val servicioId: Int,
    val concepto: String,
    val precioUnitario: Double,
    val unidadMedida: String,
    val activo: Boolean = true
)