package com.leguia.cotizadoria.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "material",
    foreignKeys = [
        ForeignKey(
            entity = ServicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["servicioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val servicioId: Int,
    val activo: Boolean = true,
    val observaciones: String = ""
)