package com.leguia.cotizadoria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.data.EventoTrazabilidadEntity
import com.leguia.cotizadoria.data.RegistroErrorEntity
import com.leguia.cotizadoria.data.ServicioEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CotizadorViewModel(
    private val repository: CotizadorRepository
) : ViewModel() {

    // El compilador infiere automáticamente el StateFlow
    val listaServiciosBaseDatos = repository.obtenerServicios()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Lista de cotizaciones registradas
    val listaCotizaciones = repository.obtenerTodasLasCotizaciones()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Guardar una nueva cotización
    fun registrarCotizacion(
        cotizacion: CotizacionEntity,
        onResultado: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val idGenerado = repository.guardarCotizacion(cotizacion)

            // Registrar trazabilidad automática
            repository.registrarEvento(
                EventoTrazabilidadEntity(
                    cotizacionId = idGenerado.toInt(),
                    tipoEvento = "CREACION_COTIZACION",
                    descripcion = "Cotización ${cotizacion.codigoCotizacion} creada exitosamente.",
                    timestamp = System.currentTimeMillis(),
                    operador = cotizacion.operador
                )
            )
            onResultado(idGenerado)
        }
    }

    // Registrar errores capturados durante la cotización
    fun capturarError(
        cotizacionId: Int,
        codigoError: String,
        descripcion: String,
        atribuibleASistema: Boolean
    ) {
        viewModelScope.launch {
            repository.registrarError(
                RegistroErrorEntity(
                    cotizacionId = cotizacionId,
                    codigoError = codigoError,
                    descripcion = descripcion,
                    atribuibleASistema = atribuibleASistema,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}