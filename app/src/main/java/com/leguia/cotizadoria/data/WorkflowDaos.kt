package com.leguia.cotizadoria.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

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
    @Query("SELECT * FROM stock WHERE materialId = :materialId LIMIT 1") suspend fun stockPorMaterial(materialId: Int): StockEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun guardarStock(entity: StockEntity): Long
    @Update suspend fun actualizarStock(entity: StockEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun registrarMovimiento(entity: MovimientoStockEntity): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun registrarRequerimiento(entity: RequerimientoMaterialEntity): Long
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun registrarNecesidad(entity: NecesidadAbastecimientoEntity): Long
    @Query("SELECT * FROM stock ORDER BY materialId") fun stock(): Flow<List<StockEntity>>
    @Query("SELECT material.nombre AS nombreMaterial, stock.cantidadDisponible AS cantidadDisponible, stock.unidad AS unidad FROM stock INNER JOIN material ON material.id = stock.materialId ORDER BY material.nombre COLLATE NOCASE")
    fun stockConMaterial(): Flow<List<StockMaterialRow>>
    @Query("SELECT * FROM requerimiento_material WHERE trabajoId = :trabajoId") fun requerimientos(trabajoId: Int): Flow<List<RequerimientoMaterialEntity>>
    @Query("SELECT * FROM necesidad_abastecimiento WHERE estado != 'CERRADA' ORDER BY creadoEn DESC") fun necesidadesAbiertas(): Flow<List<NecesidadAbastecimientoEntity>>

    @Transaction
    suspend fun registrarIngreso(nombre: String, tipo: String, cantidad: Double, unidad: String, responsable: String) {
        require(nombre.isNotBlank() && cantidad.isFinite() && cantidad > 0.0 && unidad.isNotBlank())
        val normalizedName = nombre.trim()
        val material = materialPorNombre(normalizedName)
            ?: MaterialEntity(nombre = normalizedName, tipo = tipo.trim().ifBlank { null }).let { nuevo ->
                val id = insertarMaterial(nuevo)
                nuevo.copy(id = id.toInt())
            }
        val timestamp = System.currentTimeMillis()
        val actual = stockPorMaterial(material.id)
        if (actual == null) {
            guardarStock(StockEntity(materialId = material.id, cantidadDisponible = cantidad, unidad = unidad.trim(), origen = "REGISTRO_MANUAL", actualizadoEn = timestamp))
        } else {
            require(actual.unidad.equals(unidad.trim(), ignoreCase = true)) { "La unidad debe coincidir con la registrada para el material." }
            actualizarStock(actual.copy(cantidadDisponible = actual.cantidadDisponible + cantidad, origen = "REGISTRO_MANUAL", actualizadoEn = timestamp))
        }
        registrarMovimiento(
            MovimientoStockEntity(materialId = material.id, cantidadCambio = cantidad, tipo = "INGRESO", fecha = timestamp, responsable = responsable, origen = "REGISTRO_MANUAL")
        )
    }
}

data class StockMaterialRow(val nombreMaterial: String, val cantidadDisponible: Double, val unidad: String)

@Dao
interface PrediccionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun guardar(entity: PrediccionEntity): Long
    @Query("SELECT * FROM prediccion WHERE cotizacionId = :cotizacionId ORDER BY fechaPrediccion DESC") fun porCotizacion(cotizacionId: Int): Flow<List<PrediccionEntity>>
}
