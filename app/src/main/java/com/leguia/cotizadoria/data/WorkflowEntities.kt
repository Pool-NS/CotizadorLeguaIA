package com.leguia.cotizadoria.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "proforma",
    foreignKeys = [ForeignKey(entity = CotizacionEntity::class, parentColumns = ["id"], childColumns = ["cotizacionId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["cotizacionId"], unique = true), Index("fechaCreacion")]
)
data class ProformaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cotizacionId: Int,
    val codigo: String,
    val fechaCreacion: Long,
    val responsable: String,
    val observacion: String? = null
)

@Entity(
    tableName = "trabajo_produccion",
    foreignKeys = [
        ForeignKey(entity = CotizacionEntity::class, parentColumns = ["id"], childColumns = ["cotizacionId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = ServicioEntity::class, parentColumns = ["id"], childColumns = ["servicioId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("cotizacionId"), Index("servicioId"), Index("fechaPrevista"), Index("estado")]
)
data class TrabajoProduccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cotizacionId: Int,
    val servicioId: Int,
    val fechaPrevista: Long,
    val prioridad: String,
    val estado: String = "PROGRAMADO",
    val responsable: String,
    val observaciones: String? = null
)

@Entity(
    tableName = "requerimiento_material",
    foreignKeys = [
        ForeignKey(entity = TrabajoProduccionEntity::class, parentColumns = ["id"], childColumns = ["trabajoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MaterialEntity::class, parentColumns = ["id"], childColumns = ["materialId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("trabajoId"), Index("materialId")]
)
data class RequerimientoMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trabajoId: Int,
    val materialId: Int,
    val cantidad: Double,
    val unidad: String,
    val origen: String
)

@Entity(
    tableName = "stock",
    foreignKeys = [ForeignKey(entity = MaterialEntity::class, parentColumns = ["id"], childColumns = ["materialId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["materialId"], unique = true)]
)
data class StockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materialId: Int,
    val cantidadDisponible: Double,
    val unidad: String,
    val origen: String,
    val actualizadoEn: Long
)

@Entity(
    tableName = "movimiento_stock",
    foreignKeys = [ForeignKey(entity = MaterialEntity::class, parentColumns = ["id"], childColumns = ["materialId"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("materialId"), Index("fecha"), Index("origen")]
)
data class MovimientoStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materialId: Int,
    val cantidadCambio: Double,
    val tipo: String,
    val fecha: Long,
    val responsable: String,
    val observacion: String? = null,
    val origen: String
)

@Entity(
    tableName = "necesidad_abastecimiento",
    foreignKeys = [
        ForeignKey(entity = MaterialEntity::class, parentColumns = ["id"], childColumns = ["materialId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = TrabajoProduccionEntity::class, parentColumns = ["id"], childColumns = ["trabajoId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("materialId"), Index("trabajoId"), Index("estado")]
)
data class NecesidadAbastecimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materialId: Int,
    val trabajoId: Int? = null,
    val cantidadNecesaria: Double,
    val unidad: String,
    val estado: String = "PENDIENTE_REVISION_HUMANA",
    val creadoEn: Long,
    val responsable: String
)

@Entity(
    tableName = "prediccion",
    foreignKeys = [ForeignKey(entity = CotizacionEntity::class, parentColumns = ["id"], childColumns = ["cotizacionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("cotizacionId"), Index("fechaPrediccion")]
)
data class PrediccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cotizacionId: Int,
    val probabilidadConversion: Double,
    val claseEstimada: String,
    val fechaPrediccion: Long,
    val versionModelo: String,
    val esSintetica: Boolean
)
