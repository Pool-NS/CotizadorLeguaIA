package com.leguia.cotizadoria.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leguia.cotizadoria.data.CotizacionRegistro
import com.leguia.cotizadoria.data.CotizadorOperador
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.data.EstadoCotizacion
import com.leguia.cotizadoria.data.ServicioEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CotizadorViewModel(private val repository: CotizadorRepository) : ViewModel() {

    // Flujo que lee los servicios desde la base de datos SQLite en tiempo real
    val listaServiciosBaseDatos: StateFlow<List<ServicioEntity>> = repository.serviciosActivos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var pantallaActual by mutableStateOf("INICIO")
        private set

    var operadorSeleccionado by mutableStateOf(CotizadorOperador.ANTHONY)
        private set

    var servicioSeleccionado by mutableStateOf("")
        private set

    var cotizacionActual by mutableStateOf<CotizacionRegistro?>(null)
        private set

    var esModoOffline by mutableStateOf(false)
        private set

    val listaCotizaciones = mutableStateListOf<CotizacionRegistro>()

    fun cambiarOperador(operador: CotizadorOperador) {
        operadorSeleccionado = operador
    }

    fun alternarModoRed(offline: Boolean) {
        esModoOffline = offline
    }

    fun iniciarNuevaCotizacion(servicio: String) {
        servicioSeleccionado = servicio
        val nuevaCotizacion = CotizacionRegistro(
            id = "COT-${System.currentTimeMillis().toString().takeLast(6)}",
            cotizador = operadorSeleccionado.nombreLegible,
            servicio = servicio
        )
        nuevaCotizacion.registrarEvento("inicio_cotizacion")
        cotizacionActual = nuevaCotizacion
        pantallaActual = "FORMULARIO_DATOS"
    }

    fun procesarEntradaIa(prompt: String) {
        cotizacionActual?.registrarEvento("solicitud_enviada_ia")

        if (esModoOffline) {
            cotizacionActual?.datosInterpretadosIA = "IA NO DISPONIBLE (OFFLINE)"
            cotizacionActual?.registrarEvento("fallback_offline_ia")
        } else {
            cotizacionActual?.datosInterpretadosIA = "Interpretado por IA: $prompt"
            cotizacionActual?.registrarEvento("respuesta_recibida_ia")
        }
    }

    fun guardarFormulario(resumenSpecs: String, datosManuales: String) {
        cotizacionActual?.let { reg ->
            reg.registrarEvento("ingreso_requerimiento")
            reg.caracteristicasIngresadas = resumenSpecs
            reg.datosCorregidosManual = datosManuales
            reg.registrarEvento("datos_confirmados")
            reg.precioReferencial = 150.00
            reg.registrarEvento("precio_referencial_generado")
        }
        pantallaActual = "RESUMEN_PRECIO"
    }

    fun finalizarCotizacion(descuento: Double, precioFinal: Double, observaciones: String) {
        cotizacionActual?.let { reg ->
            reg.descuento = descuento
            reg.registrarEvento("descuento_registrado")
            reg.precioFinal = precioFinal
            reg.observaciones = observaciones
            reg.horaFin = System.currentTimeMillis()
            reg.estado = EstadoCotizacion.CONFIRMADA
            reg.registrarEvento("cotizacion_finalizada")

            listaCotizaciones.add(reg)
        }
        pantallaActual = "INICIO"
    }

    fun eliminarCotizacionConPin(cotizacion: CotizacionRegistro) {
        listaCotizaciones.remove(cotizacion)
    }

    fun navegarA(pantalla: String) {
        pantallaActual = pantalla
    }
}