package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import com.leguia.cotizadoria.domain.StockMovementRules

@Dao
interface ProformaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertar(entity: ProformaEntity): Long
    @Query("SELECT * FROM proforma WHERE cotizacionId = :cotizacionId") suspend fun porCotizacion(cotizacionId: Int): ProformaEntity?
    @Query("SELECT * FROM proforma ORDER BY fechaCreacion DESC") fun todas(): Flow<List<ProformaEntity>>
}

@Dao
interface TrabajoProduccionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertar(entity: TrabajoProduccionEntity): Long
    @Update suspend fun actualizar(entity: TrabajoProduccionEntity)
    @Query("SELECT * FROM trabajo_produccion ORDER BY fechaPrevista ASC") fun todos(): Flow<List<TrabajoProduccionEntity>>
    @Query("SELECT * FROM trabajo_produccion WHERE estado = :estado ORDER BY fechaPrevista ASC") fun porEstado(estado: String): Flow<List<TrabajoProduccionEntity>>
}

@Dao
interface InventarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertarMaterial(entity: MaterialEntity): Long
    @Query("SELECT * FROM material WHERE nombre = :nombre COLLATE NOCASE LIMIT 1") suspend fun materialPorNombre(nombre: String): MaterialEntity?
    @Update suspend fun actualizarMaterial(entity: MaterialEntity)
    @Query("SELECT * FROM stock WHERE materialId = :materialId LIMIT 1") suspend fun stockPorMaterial(materialId: Int): StockEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun guardarStock(entity: StockEntity): Long
    @Update suspend fun actualizarStock(entity: StockEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun registrarMovimiento(entity: MovimientoStockEntity): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun registrarRequerimiento(entity: RequerimientoMaterialEntity): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun registrarNecesidad(entity: NecesidadAbastecimientoEntity): Long
    @Query("SELECT * FROM stock ORDER BY materialId") fun stock(): Flow<List<StockEntity>>
    @Query("SELECT material.nombre AS nombreMaterial, material.tipo AS categoria, stock.cantidadDisponible AS cantidadDisponible, stock.unidad AS unidad, stock.medidaPresentacion AS medidaPresentacion FROM stock INNER JOIN material ON material.id = stock.materialId ORDER BY material.nombre COLLATE NOCASE")
    fun stockConMaterial(): Flow<List<StockMaterialRow>>
    @Query("SELECT material.nombre AS nombreMaterial, stock.unidad AS unidad, movimiento_stock.cantidadCambio AS cantidadCambio, movimiento_stock.tipo AS tipo, movimiento_stock.fecha AS fecha, movimiento_stock.responsable AS responsable, movimiento_stock.observacion AS observacion FROM movimiento_stock INNER JOIN material ON material.id = movimiento_stock.materialId INNER JOIN stock ON stock.materialId = material.id ORDER BY movimiento_stock.fecha DESC LIMIT 50")
    fun movimientosRecientes(): Flow<List<StockMovementRow>>
    @Query("SELECT * FROM requerimiento_material WHERE trabajoId = :trabajoId") fun requerimientos(trabajoId: Int): Flow<List<RequerimientoMaterialEntity>>
    @Query("SELECT * FROM necesidad_abastecimiento WHERE estado != 'CERRADA' ORDER BY creadoEn DESC") fun necesidadesAbiertas(): Flow<List<NecesidadAbastecimientoEntity>>

    @Transaction
    suspend fun registrarIngreso(nombre: String, tipo: String, cantidad: Double, unidad: String, medidaPresentacion: String?, motivo: String, responsable: String) {
        require(nombre.isNotBlank() && cantidad.isFinite() && cantidad > 0.0 && unidad.isNotBlank() && motivo.isNotBlank())
        val normalizedName = nombre.trim()
        val materialExistente = materialPorNombre(normalizedName)
        val material = materialExistente
            ?: MaterialEntity(nombre = normalizedName, tipo = tipo.trim().ifBlank { null }).let { nuevo ->
                val id = insertarMaterial(nuevo)
                nuevo.copy(id = id.toInt())
            }
        if (materialExistente != null && material.tipo.isNullOrBlank() && tipo.isNotBlank()) {
            actualizarMaterial(material.copy(tipo = tipo.trim()))
        }
        val timestamp = System.currentTimeMillis()
        val actual = stockPorMaterial(material.id)
        if (actual == null) {
            guardarStock(StockEntity(materialId = material.id, cantidadDisponible = cantidad, unidad = unidad.trim(), medidaPresentacion = medidaPresentacion?.trim()?.takeIf(String::isNotBlank), origen = "REGISTRO_MANUAL", actualizadoEn = timestamp))
        } else {
            StockMovementRules.requireSameUnit(actual.unidad, unidad)
            val incomingMeasure = medidaPresentacion?.trim()?.takeIf(String::isNotBlank)
            require(actual.medidaPresentacion.isNullOrBlank() || incomingMeasure.isNullOrBlank() || actual.medidaPresentacion.equals(incomingMeasure, ignoreCase = true)) {
                "La medida debe coincidir con la registrada para este material. Usa otro nombre si es una presentación distinta."
            }
            actualizarStock(actual.copy(cantidadDisponible = actual.cantidadDisponible + cantidad, medidaPresentacion = actual.medidaPresentacion ?: incomingMeasure, origen = "REGISTRO_MANUAL", actualizadoEn = timestamp))
        }
        registrarMovimiento(
            MovimientoStockEntity(materialId = material.id, cantidadCambio = cantidad, tipo = "INGRESO", fecha = timestamp, responsable = responsable, observacion = motivo.trim().take(160), origen = "REGISTRO_MANUAL")
        )
    }

    @Transaction
    suspend fun registrarAjuste(nombre: String, cambio: Double, unidad: String, tipoMovimiento: String, motivo: String, responsable: String) {
        require(nombre.isNotBlank() && cambio.isFinite() && cambio != 0.0 && unidad.isNotBlank() && motivo.isNotBlank())
        require(tipoMovimiento == "SALIDA" || tipoMovimiento == "AJUSTE")
        val material = materialPorNombre(nombre.trim()) ?: error("El material no existe en el inventario.")
        val current = stockPorMaterial(material.id) ?: error("El material no tiene saldo registrado.")
        StockMovementRules.requireSameUnit(current.unidad, unidad)
        val delta = StockMovementRules.signedDelta(tipoMovimiento, cambio)
        val balance = StockMovementRules.resultingBalance(current.cantidadDisponible, delta)
        val now = System.currentTimeMillis()
        actualizarStock(current.copy(cantidadDisponible = balance, origen = "AJUSTE_MANUAL", actualizadoEn = now))
        registrarMovimiento(
            MovimientoStockEntity(
                materialId = material.id,
                cantidadCambio = delta,
                tipo = tipoMovimiento,
                fecha = now,
                responsable = responsable,
                observacion = motivo.trim().take(160),
                origen = "AJUSTE_MANUAL"
            )
        )
    }
}

data class StockMaterialRow(
    val nombreMaterial: String,
    val cantidadDisponible: Double,
    val unidad: String,
    val medidaPresentacion: String? = null,
    val categoria: String? = null
)
data class StockMovementRow(val nombreMaterial: String, val unidad: String, val cantidadCambio: Double, val tipo: String, val fecha: Long, val responsable: String, val observacion: String?)

@Dao
interface PrediccionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun guardar(entity: PrediccionEntity): Long
    @Query("SELECT * FROM prediccion WHERE cotizacionId = :cotizacionId ORDER BY fechaPrediccion DESC") fun porCotizacion(cotizacionId: Int): Flow<List<PrediccionEntity>>
}
