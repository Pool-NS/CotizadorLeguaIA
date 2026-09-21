package com.leguia.cotizadoria.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servicio")
data class ServicioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val activo: Boolean = true
)