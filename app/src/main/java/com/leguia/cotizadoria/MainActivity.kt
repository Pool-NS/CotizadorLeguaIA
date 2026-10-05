package com.leguia.cotizadoria

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.leguia.cotizadoria.ai.BackendGenerativeAiClient
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.domain.SpokenRequirementParser
import com.leguia.cotizadoria.domain.ConfiguredPrice
import com.leguia.cotizadoria.domain.PriceLine
import com.leguia.cotizadoria.domain.PricingOutcome
import com.leguia.cotizadoria.domain.QuotePricingCalculator
import com.leguia.cotizadoria.domain.DimensionPriceKey
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
            registroErrorDao = db.registroErrorDao(),
            proformaDao = db.proformaDao(),
            trabajoProduccionDao = db.trabajoProduccionDao(),
            inventarioDao = db.inventarioDao(),
            prediccionDao = db.prediccionDao()
        )
        val viewModel = CotizadorViewModel(repository, BackendGenerativeAiClient(BuildConfig.AI_BACKEND_URL))

        setContent {
            var pantallaActual by remember { mutableStateOf(DestinoPantalla.INICIO) }

            var operadorActual by remember { mutableStateOf("Operador 1") }
            var esModoOffline by remember { mutableStateOf(false) }
            var servicioIdSeleccionado by remember { mutableIntStateOf(1) }
            var transcripcionVozInicial by remember { mutableStateOf<String?>(null) }
            var mensajeVoz by remember { mutableStateOf<String?>(null) }

            // Definición correcta del estado mutable opcional con Kotlin Compose
            var cotizacionGenerada: CotizacionEntity? by remember { mutableStateOf(null) }

            val listaServicios by viewModel.listaServiciosBaseDatos.collectAsState()
            val listaCotizaciones by viewModel.listaCotizaciones.collectAsState()
            val listaStock by viewModel.listaStock.collectAsState()
            val tarifasActivas by viewModel.tarifasActivas.collectAsState()
            val aiInterpretationState by viewModel.aiInterpretationState.collectAsState()
            val reconocedorLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { resultado ->
                val transcripcion = resultado.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                    ?.trim()
                    .orEmpty()
                if (resultado.resultCode != Activity.RESULT_OK || transcripcion.isBlank()) {
                    mensajeVoz = "No se recibió una transcripción. Puedes intentarlo otra vez o elegir el servicio manualmente."
                } else {
                    val interpretacion = SpokenRequirementParser.interpret(transcripcion)
                    val servicioDetectado = interpretacion.service?.let { nombre ->
                        listaServicios.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }
                    }
                    if (servicioDetectado == null) {
                        mensajeVoz = if (interpretacion.ambiguousServices.isNotEmpty()) {
                            "Mencionaste más de un servicio (${interpretacion.ambiguousServices.joinToString()}). Elige uno manualmente."
                        } else "No identifiqué Carpas, Toldos o Tapizado. Elige el servicio manualmente."
                    } else {
                        mensajeVoz = null
                        servicioIdSeleccionado = servicioDetectado.id
                        transcripcionVozInicial = transcripcion
                        pantallaActual = DestinoPantalla.FORMULARIO_SERVICIO
                    }
                }
            }

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
                                listaStock = listaStock,
                                servicios = listaServicios,
                                tarifas = tarifasActivas,
                                onGuardarTarifa = { servicioId, largo, ancho, variante, material, precio ->
                                    viewModel.guardarTarifaAprobada(servicioId, largo, ancho, variante, material, precio) { error ->
                                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                                    }
                                },
                                onRegistrarIngreso = { nombre, tipo, cantidad, unidad, resultado ->
                                    viewModel.registrarIngresoMaterial(nombre, tipo, cantidad, unidad, operadorActual, resultado)
                                },
                                onRegistrarResultado = { cotizacion, resultado ->
                                    viewModel.registrarResultadoComercial(cotizacion, operadorActual, resultado)
                                },
                                onVolver = { pantallaActual = DestinoPantalla.INICIO }
                            )
                        }

                        DestinoPantalla.SELECCION_SERVICIO -> {
                            PantallaSeleccionServicio(
                                listaServicios = listaServicios,
                                onServicioSeleccionado = { servicio ->
                                    viewModel.limpiarInterpretacionIa()
                                    servicioIdSeleccionado = servicio.id
                                    transcripcionVozInicial = null
                                    pantallaActual = DestinoPantalla.FORMULARIO_SERVICIO
                                },
                                onCapturarPorVoz = {
                                    mensajeVoz = null
                                    viewModel.limpiarInterpretacionIa()
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Di el servicio, el largo y el ancho; por ejemplo: carpa de 3 por 2")
                                    }
                                    try {
                                        reconocedorLauncher.launch(intent)
                                    } catch (_: android.content.ActivityNotFoundException) {
                                        mensajeVoz = "Este dispositivo no tiene un servicio de reconocimiento de voz disponible."
                                    }
                                },
                                mensajeVoz = mensajeVoz,
                                onVolver = { pantallaActual = DestinoPantalla.INICIO }
                            )
                        }

                        DestinoPantalla.FORMULARIO_SERVICIO -> {
                            PantallaFormularioServicio(
                                servicioId = servicioIdSeleccionado,
                                servicioNombre = listaServicios.firstOrNull { it.id == servicioIdSeleccionado }?.nombre.orEmpty(),
                                operador = operadorActual,
                                esModoOffline = esModoOffline,
                                transcripcionInicial = transcripcionVozInicial,
                                interpretacionIA = aiInterpretationState.result,
                                interpretacionIALoading = aiInterpretationState.loading,
                                errorIA = aiInterpretationState.error,
                                onInterpretarRequerimiento = viewModel::interpretarRequerimiento,
                                onGuardarFormulario = { cotizacionIngresada ->
                                    val priceKey = cotizacionIngresada.medidaLargo?.let { length ->
                                        cotizacionIngresada.medidaAncho?.let { width ->
                                            val variant = cotizacionIngresada.tipoCarpa ?: "BASE"
                                            runCatching {
                                                DimensionPriceKey.forSize(length, width, variant, cotizacionIngresada.materialDescripcion.orEmpty())
                                            }.getOrNull()
                                        }
                                    }
                                    val activeRate = tarifasActivas.firstOrNull {
                                        it.servicioId == cotizacionIngresada.servicioId && it.concepto == priceKey
                                    }
                                    val calculated = if (activeRate != null) {
                                        QuotePricingCalculator.calculate(
                                            listOf(PriceLine(ConfiguredPrice(activeRate.precioUnitario, activeRate.unidadMedida), 1.0)),
                                            humanDiscount = 0.0
                                        )
                                    } else null
                                    cotizacionGenerada = when (val outcome = calculated) {
                                        is PricingOutcome.Calculated -> cotizacionIngresada.copy(
                                            precioReferencial = outcome.result.reference,
                                            precioFinal = outcome.result.final,
                                            estado = "PENDIENTE"
                                        )
                                        else -> cotizacionIngresada.copy(precioReferencial = 0.0, precioFinal = 0.0, estado = "PENDIENTE_PRECIO")
                                    }
                                    pantallaActual = DestinoPantalla.RESUMEN_PRECIO
                                },
                                onVolver = {
                                    viewModel.limpiarInterpretacionIa()
                                    pantallaActual = DestinoPantalla.SELECCION_SERVICIO
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
                                medidaAlto = null,
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
                                servicioNombre = listaServicios.firstOrNull { it.id == cotizacionValida.servicioId }?.nombre.orEmpty(),
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
