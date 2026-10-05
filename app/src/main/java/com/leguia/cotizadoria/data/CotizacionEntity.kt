package com.leguia.cotizadoria.data


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cotizacion",
    foreignKeys = [
        ForeignKey(
            entity = ServicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["servicioId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["codigoCotizacion"], unique = true),
        Index("servicioId"),
        Index("materialId"),
        Index("estado"),
        Index("fechaHoraInicio")
    ]
)
data class CotizacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigoCotizacion: String,
    val servicioId: Int,
    val materialId: Int?,
    val operador: String,
    val requerimientoCliente: String,
    val interpretacionIaJson: String?,
    val corregidoPorHumano: Boolean,
    val medidaLargo: Double?,
    val medidaAncho: Double?,
    val medidaAlto: Double?,
    val precioReferencial: Double,
    val descuentoMonto: Double,
    val descuentoPorcentaje: Double,
    val precioFinal: Double,
    val estado: String,
    val fechaHoraInicio: Long,
    val fechaHoraFin: Long?,
    val esModoOffline: Boolean,
    val tipoMoto: String? = null,
    val tipoCarpa: String? = null,
    val cantidadVentanas: Int? = null,
    val cantidadPuertas: Int? = null,
    val materialDescripcion: String? = null
)
