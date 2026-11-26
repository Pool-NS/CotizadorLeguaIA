package com.leguia.cotizadoria.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import com.leguia.cotizadoria.ai.GenerativeAiClient
import com.leguia.cotizadoria.ai.RequirementInterpretation
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.data.RegistroErrorEntity
import com.leguia.cotizadoria.data.ServicioEntity
import com.leguia.cotizadoria.domain.QuoteStatus
import com.leguia.cotizadoria.domain.QuoteWorkflow
import com.leguia.cotizadoria.firebase.NoOpTraceRecorder
import com.leguia.cotizadoria.firebase.TraceRecorder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CotizadorViewModel(
    private val repository: CotizadorRepository,
    private val generativeAiClient: GenerativeAiClient,
    private val traceRecorder: TraceRecorder = NoOpTraceRecorder
) : ViewModel() {

    data class AiInterpretationState(
        val loading: Boolean = false,
        val result: RequirementInterpretation? = null,
        val error: String? = null
    )

    private val _aiInterpretationState = MutableStateFlow(AiInterpretationState())
    val aiInterpretationState = _aiInterpretationState.asStateFlow()

    fun interpretarRequerimiento(requirement: String) {
        viewModelScope.launch {
            _aiInterpretationState.value = AiInterpretationState(loading = true)
            generativeAiClient.interpret(requirement)
                .onSuccess { interpretation ->
                    _aiInterpretationState.value = AiInterpretationState(result = interpretation)
                }
                .onFailure { failure ->
                    _aiInterpretationState.value = AiInterpretationState(
                        error = failure.message ?: "No se pudo interpretar el requerimiento."
                    )
                }
        }
    }

    fun limpiarInterpretacionIa() {
        _aiInterpretationState.value = AiInterpretationState()
    }

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

    val movimientosStock = repository.obtenerMovimientosStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tarifasActivas = repository.obtenerTodosLosParametrosActivos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historialTarifas = repository.obtenerHistorialTarifas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardarTarifaAprobada(servicioId: Int, largo: Double, ancho: Double, variant: String, material: String, precio: Double, onError: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.guardarTarifaAprobada(servicioId, largo, ancho, variant, material, precio) }
                .onFailure { onError(it.message ?: "No se pudo guardar la tarifa.") }
        }
    }

    fun registrarIngresoMaterial(nombre: String, tipo: String, cantidad: Double, unidad: String, medidaPresentacion: String?, motivo: String, operador: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.registrarIngresoMaterial(nombre, tipo, cantidad, unidad, medidaPresentacion, motivo, operador) }
                .onSuccess { onResult(null) }
                .onFailure { onResult(it.message ?: "No se pudo registrar el ingreso de material.") }
        }
    }

    fun registrarAjusteStock(nombre: String, cambio: Double, unidad: String, tipoMovimiento: String, motivo: String, operador: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            runCatching { repository.registrarAjusteStock(nombre, cambio, unidad, tipoMovimiento, motivo, operador) }
                .onSuccess { onResult(null) }
                .onFailure { onResult(it.message ?: "No se pudo registrar el movimiento.") }
        }
    }

    // Guardar una nueva cotización
    fun registrarCotizacion(
        cotizacion: CotizacionEntity,
        onResultado: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val trace = traceRecorder.start("quote_local_save")
            val startedAt = SystemClock.elapsedRealtime()
            try {
                val idGenerado = repository.guardarCotizacion(cotizacion)
                trace.putMetric("save_succeeded", 1L)
                onResultado(idGenerado)
            } catch (failure: Exception) {
                trace.putMetric("save_succeeded", 0L)
                throw failure
            } finally {
                trace.putMetric("duration_ms", SystemClock.elapsedRealtime() - startedAt)
                trace.stop()
            }
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
