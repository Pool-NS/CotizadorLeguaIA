package com.leguia.cotizadoria.data


import kotlinx.coroutines.flow.Flow
import com.leguia.cotizadoria.domain.DimensionPriceKey

class CotizadorRepository(
    private val servicioDao: ServicioDao,
    private val cotizacionDao: CotizacionDao,
    private val parametroPrecioDao: ParametroPrecioDao,
    private val trazabilidadDao: TrazabilidadDao,
    private val registroErrorDao: RegistroErrorDao,
    private val proformaDao: ProformaDao,
    private val trabajoProduccionDao: TrabajoProduccionDao,
    private val inventarioDao: InventarioDao,
    private val prediccionDao: PrediccionDao
) {
    // Servicios
    fun obtenerServicios(): Flow< List< ServicioEntity > > = servicioDao.obtenerTodos()
    suspend fun insertarServicio(servicio: ServicioEntity) = servicioDao.insertar(servicio)

    // Cotizaciones
    fun obtenerTodasLasCotizaciones(): Flow< List< CotizacionEntity > > = cotizacionDao.obtenerTodas()
    suspend fun guardarCotizacion(cotizacion: CotizacionEntity): Long = cotizacionDao.registrarCotizacionConEvento(cotizacion)
    suspend fun obtenerCotizacionPorId(id: Int): CotizacionEntity? = cotizacionDao.obtenerPorId(id)

    suspend fun cambiarEstadoCotizacion(cotizacion: CotizacionEntity, actor: String, nuevoEstado: String, nota: String?) {
        require(cotizacion.estado == "PENDIENTE" && nuevoEstado in setOf("CONCRETADA", "NO_CONCRETADA")) {
            "Transición comercial no permitida"
        }
        val timestamp = System.currentTimeMillis()
        cotizacionDao.actualizarEstadoConEvento(
            cotizacion.copy(estado = nuevoEstado, fechaHoraFin = timestamp),
            EventoTrazabilidadEntity(
                cotizacionId = cotizacion.id,
                tipoEvento = "CAMBIO_ESTADO_${nuevoEstado}",
                descripcion = nota.orEmpty(),
                timestamp = timestamp,
                operador = actor
            )
        )
    }

    fun obtenerProformas() = proformaDao.todas()
    suspend fun registrarProforma(entity: ProformaEntity): Long {
        require(cotizacionDao.obtenerPorId(entity.cotizacionId)?.estado == "CONCRETADA") {
            "Solo una cotización concretada puede tener proforma"
        }
        return proformaDao.insertar(entity)
    }
    fun obtenerTrabajos() = trabajoProduccionDao.todos()
    suspend fun registrarTrabajo(entity: TrabajoProduccionEntity): Long {
        require(cotizacionDao.obtenerPorId(entity.cotizacionId)?.estado == "CONCRETADA") {
            "La planificación requiere una cotización concretada"
        }
        return trabajoProduccionDao.insertar(entity)
    }
    fun obtenerStock() = inventarioDao.stock()
    fun obtenerStockConMaterial() = inventarioDao.stockConMaterial()
    fun obtenerMovimientosStock() = inventarioDao.movimientosRecientes()
    suspend fun registrarIngresoMaterial(nombre: String, tipo: String, cantidad: Double, unidad: String, medidaPresentacion: String?, motivo: String, responsable: String) =
        inventarioDao.registrarIngreso(nombre, tipo, cantidad, unidad, medidaPresentacion, motivo, responsable)
    suspend fun registrarAjusteStock(nombre: String, cambio: Double, unidad: String, tipoMovimiento: String, motivo: String, responsable: String) =
        inventarioDao.registrarAjuste(nombre, cambio, unidad, tipoMovimiento, motivo, responsable)
    suspend fun guardarStock(entity: StockEntity) = inventarioDao.guardarStock(entity)
    suspend fun registrarMovimientoStock(entity: MovimientoStockEntity) = inventarioDao.registrarMovimiento(entity)
    suspend fun registrarNecesidadAbastecimiento(entity: NecesidadAbastecimientoEntity) = inventarioDao.registrarNecesidad(entity)
    suspend fun registrarPrediccion(entity: PrediccionEntity) = prediccionDao.guardar(entity)

    // Parámetros de precio
    fun obtenerParametrosPorServicio(servicioId: Int): Flow< List< ParametroPrecioEntity > > =
        parametroPrecioDao.obtenerPorServicio(servicioId)
    fun obtenerTodosLosParametrosActivos() = parametroPrecioDao.obtenerTodosActivos()
    fun obtenerHistorialTarifas() = parametroPrecioDao.obtenerHistorialCompleto()
    suspend fun guardarTarifaAprobada(servicioId: Int, largo: Double, ancho: Double, variant: String, material: String, precio: Double) =
        parametroPrecioDao.guardarTarifaAprobada(servicioId, DimensionPriceKey.forSize(largo, ancho, variant, material), precio)

    // Trazabilidad y Errores
    suspend fun registrarEvento(evento: EventoTrazabilidadEntity) = trazabilidadDao.insertarEvento(evento)
    suspend fun registrarError(error: RegistroErrorEntity) = registroErrorDao.registrarError(error)
}
