package com.leguia.cotizadoria.data


import kotlinx.coroutines.flow.Flow

class CotizadorRepository(private val servicioDao: ServicioDao) {

    val serviciosActivos: Flow<List<ServicioEntity>> = servicioDao.obtenerServiciosActivos()
}