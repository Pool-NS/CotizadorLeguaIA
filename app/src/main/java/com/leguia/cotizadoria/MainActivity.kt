package com.leguia.cotizadoria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import com.leguia.cotizadoria.data.AppDatabase
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.screens.PantallaFormularioServicio
import com.leguia.cotizadoria.screens.PantallaInicio
import com.leguia.cotizadoria.screens.PantallaLoginAdmin
import com.leguia.cotizadoria.screens.PantallaPanelAdmin
import com.leguia.cotizadoria.screens.PantallaResumenPrecio
import com.leguia.cotizadoria.screens.PantallaSeleccionServicio
import com.leguia.cotizadoria.viewmodel.CotizadorViewModel

enum class DestinoPantalla {
    INICIO,
    LOGIN_ADMIN,
    PANEL_ADMIN,
    SELECCION_SERVICIO,
    FORMULARIO_SERVICIO,
    RESUMEN_PRECIO
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = CotizadorRepository(
            servicioDao = db.servicioDao(),
            cotizacionDao = db.cotizacionDao(),
            parametroPrecioDao = db.parametroPrecioDao(),
            trazabilidadDao = db.trazabilidadDao(),
            registroErrorDao = db.registroErrorDao()
        )
        val viewModel = CotizadorViewModel(repository)

        setContent {
            var pantallaActual by remember { mutableStateOf(DestinoPantalla.INICIO) }

            var operadorActual by remember { mutableStateOf("Operador 1") }
            var esModoOffline by remember { mutableStateOf(false) }
            var servicioIdSeleccionado by remember { mutableIntStateOf(1) }

            // Definición correcta del estado mutable opcional con Kotlin Compose
            var cotizacionGenerada: CotizacionEntity? by remember { mutableStateOf(null) }

            val listaServicios by viewModel.listaServiciosBaseDatos.collectAsState()
            val listaCotizaciones by viewModel.listaCotizaciones.collectAsState()

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (pantallaActual) {
                        DestinoPantalla.INICIO -> {
                            PantallaInicio(
                                operadorSeleccionado = operadorActual,
                                esModoOffline = esModoOffline,
                                onCambiarOperador = { nuevoOperador -> operadorActual = nuevoOperador },
                                onAlternarModoRed = { esModoOffline = !esModoOffline },
                                onIniciarNuevaCotizacion = { pantallaActual = DestinoPantalla.SELECCION_SERVICIO },
                                onIrAPanelAdmin = { pantallaActual = DestinoPantalla.LOGIN_ADMIN }
                            )
                        }

                        DestinoPantalla.LOGIN_ADMIN -> {
                            PantallaLoginAdmin(
                                onAccesoConcedido = { pantallaActual = DestinoPantalla.PANEL_ADMIN },
                                onVolver = { pantallaActual = DestinoPantalla.INICIO }
                            )
                        }

                        DestinoPantalla.PANEL_ADMIN -> {
                            PantallaPanelAdmin(
                                listaCotizaciones = listaCotizaciones,
                                onVolver = { pantallaActual = DestinoPantalla.INICIO }
                            )
                        }

                        DestinoPantalla.SELECCION_SERVICIO -> {
                            PantallaSeleccionServicio(
                                listaServicios = listaServicios,
                                onServicioSeleccionado = { servicio ->
                                    servicioIdSeleccionado = servicio.id
                                    pantallaActual = DestinoPantalla.FORMULARIO_SERVICIO
                                },
                                onVolver = { pantallaActual = DestinoPantalla.INICIO }
                            )
                        }

                        DestinoPantalla.FORMULARIO_SERVICIO -> {
                            PantallaFormularioServicio(
                                servicioId = servicioIdSeleccionado,
                                operador = operadorActual,
                                onGuardarFormulario = { cotizacionIngresada ->
                                    val largo = cotizacionIngresada.medidaLargo ?: 1.0
                                    val ancho = cotizacionIngresada.medidaAncho ?: 1.0
                                    val alto = cotizacionIngresada.medidaAlto ?: 1.0
                                    val tarifaCalculada = (largo * ancho * alto) * 15.0

                                    // Asignación directa limpia
                                    cotizacionGenerada = cotizacionIngresada.copy(
                                        precioReferencial = tarifaCalculada,
                                        precioFinal = tarifaCalculada
                                    )
                                    pantallaActual = DestinoPantalla.RESUMEN_PRECIO
                                }
                            )
                        }

                        DestinoPantalla.RESUMEN_PRECIO -> {
                            val cotizacionValida = cotizacionGenerada ?: CotizacionEntity(
                                id = 0,
                                codigoCotizacion = "COT-000",
                                servicioId = servicioIdSeleccionado,
                                materialId = null,
                                operador = operadorActual,
                                requerimientoCliente = "Sin datos registrados",
                                interpretacionIaJson = null,
                                corregidoPorHumano = false,
                                medidaLargo = 0.0,
                                medidaAncho = 0.0,
                                medidaAlto = 0.0,
                                precioReferencial = 0.0,
                                descuentoMonto = 0.0,
                                descuentoPorcentaje = 0.0,
                                precioFinal = 0.0,
                                estado = "PENDIENTE",
                                fechaHoraInicio = System.currentTimeMillis(),
                                fechaHoraFin = null,
                                esModoOffline = esModoOffline
                            )

                            PantallaResumenPrecio(
                                cotizacion = cotizacionValida,
                                onFinalizarCotizacion = {
                                    viewModel.registrarCotizacion(cotizacionValida) {
                                        pantallaActual = DestinoPantalla.INICIO
                                    }
                                },
                                onVolver = { pantallaActual = DestinoPantalla.FORMULARIO_SERVICIO }
                            )
                        }
                    }
                }
            }
        }
    }
}