package com.leguia.cotizadoria.data


import kotlinx.coroutines.flow.Flow

class CotizadorRepository(
    private val servicioDao: ServicioDao,
    private val cotizacionDao: CotizacionDao,
    private val parametroPrecioDao: ParametroPrecioDao,
    private val trazabilidadDao: TrazabilidadDao,
    private val registroErrorDao: RegistroErrorDao
) {
    // Servicios
    fun obtenerServicios(): Flow< List< ServicioEntity > > = servicioDao.obtenerTodos()
    suspend fun insertarServicio(servicio: ServicioEntity) = servicioDao.insertar(servicio)

    // Cotizaciones
    fun obtenerTodasLasCotizaciones(): Flow< List< CotizacionEntity > > = cotizacionDao.obtenerTodas()
    suspend fun guardarCotizacion(cotizacion: CotizacionEntity): Long = cotizacionDao.insertarCotizacion(cotizacion)
    suspend fun obtenerCotizacionPorId(id: Int): CotizacionEntity? = cotizacionDao.obtenerPorId(id)

    // Parámetros de precio
    fun obtenerParametrosPorServicio(servicioId: Int): Flow< List< ParametroPrecioEntity > > =
        parametroPrecioDao.obtenerPorServicio(servicioId)

    // Trazabilidad y Errores
    suspend fun registrarEvento(evento: EventoTrazabilidadEntity) = trazabilidadDao.insertarEvento(evento)
    suspend fun registrarError(error: RegistroErrorEntity) = registroErrorDao.registrarError(error)
}