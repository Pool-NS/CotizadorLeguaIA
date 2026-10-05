package com.leguia.cotizadoria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.data.RegistroErrorEntity
import com.leguia.cotizadoria.data.ServicioEntity
import com.leguia.cotizadoria.domain.QuoteStatus
import com.leguia.cotizadoria.domain.QuoteWorkflow
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

    val listaStock = repository.obtenerStockConMaterial()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tarifasActivas = repository.obtenerTodosLosParametrosActivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarTarifaAprobada(servicioId: Int, largo: Double, ancho: Double, variant: String, precio: Double, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.guardarTarifaAprobada(servicioId, largo, ancho, variant, precio) }
                .onFailure { onError(it.message ?: "No se pudo guardar la tarifa.") }
        }
    }

    fun registrarIngresoMaterial(nombre: String, tipo: String, cantidad: Double, unidad: String, operador: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.registrarIngresoMaterial(nombre, tipo, cantidad, unidad, operador) }
                .onSuccess { onResult(null) }
                .onFailure { onResult(it.message ?: "No se pudo registrar el ingreso de material.") }
        }
    }

    // Guardar una nueva cotización
    fun registrarCotizacion(
        cotizacion: CotizacionEntity,
        onResultado: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val idGenerado = repository.guardarCotizacion(cotizacion)

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

    fun registrarResultadoComercial(cotizacion: CotizacionEntity, actor: String, resultado: QuoteStatus) {
        viewModelScope.launch {
            val currentStatus = runCatching { QuoteStatus.valueOf(cotizacion.estado) }.getOrNull() ?: return@launch
            val transition = QuoteWorkflow.transition(
                from = currentStatus,
                to = resultado,
                actor = actor,
                atMillis = System.currentTimeMillis()
            )
            transition.onSuccess {
                repository.cambiarEstadoCotizacion(cotizacion, it.actor, it.to.name, it.note)
            }
        }
    }
}
