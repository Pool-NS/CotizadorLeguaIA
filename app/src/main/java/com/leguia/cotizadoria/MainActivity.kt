package com.leguia.cotizadoria

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import com.leguia.cotizadoria.data.AppDatabase
import com.leguia.cotizadoria.ai.BackendGenerativeAiClient
import kotlinx.coroutines.Dispatchers
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.ParametroPrecioEntity
import com.leguia.cotizadoria.data.ServicioEntity
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.domain.SpokenRequirementParser
import com.leguia.cotizadoria.domain.ConfiguredPrice
import com.leguia.cotizadoria.domain.PriceLine
import com.leguia.cotizadoria.domain.PricingOutcome
import com.leguia.cotizadoria.domain.QuotePricingCalculator
import com.leguia.cotizadoria.domain.DimensionPriceKey
import com.leguia.cotizadoria.domain.QuoteCsvExport
import com.leguia.cotizadoria.screens.PantallaFormularioServicio
import com.leguia.cotizadoria.screens.PantallaInicio
import com.leguia.cotizadoria.screens.PantallaLoginAdmin
import com.leguia.cotizadoria.screens.PantallaAccesoNube
import com.leguia.cotizadoria.screens.PantallaPanelAdmin
import com.leguia.cotizadoria.screens.PantallaResumenPrecio
import com.leguia.cotizadoria.screens.PantallaSeleccionServicio
import com.leguia.cotizadoria.viewmodel.CotizadorViewModel
import com.leguia.cotizadoria.firebase.FirebaseCloudRepository
import com.leguia.cotizadoria.firebase.FirebaseTraceRecorder
import com.leguia.cotizadoria.firebase.NoOpTraceRecorder
import com.leguia.cotizadoria.firebase.FirebaseSyncScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

enum class DestinoPantalla {
    INICIO,
    LOGIN_ADMIN,
    PANEL_ADMIN,
    ACCESO_NUBE,
    SELECCION_SERVICIO,
    FORMULARIO_SERVICIO,
    RESUMEN_PRECIO
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val firebaseCloud = if (BuildConfig.FIREBASE_ENABLED) {
            FirebaseCloudRepository(
                context = applicationContext,
                auth = FirebaseAuth.getInstance(),
                firestore = FirebaseFirestore.getInstance(),
                quoteDao = db.cotizacionDao(),
                organizationId = BuildConfig.FIREBASE_ORGANIZATION_ID,
                appVersion = BuildConfig.VERSION_NAME
            )
        } else null
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
        val traceRecorder = if (BuildConfig.FIREBASE_ENABLED) FirebaseTraceRecorder() else NoOpTraceRecorder
        val viewModel = CotizadorViewModel(
            repository,
            BackendGenerativeAiClient(
                BuildConfig.AI_BACKEND_URL,
                Dispatchers.IO,
                traceRecorder
            ),
            traceRecorder
        )

        setContent {
            var pantallaActual by remember { mutableStateOf(DestinoPantalla.INICIO) }

            var operadorActual by remember { mutableStateOf("Operador 1") }
            var esModoOffline by remember { mutableStateOf(false) }
            var servicioIdSeleccionado by remember { mutableIntStateOf(1) }
            var inicioCotizacionMillis by remember { mutableLongStateOf(0L) }
            var transcripcionVozInicial by remember { mutableStateOf<String?>(null) }
            var mensajeVoz by remember { mutableStateOf<String?>(null) }
            var cloudSignedIn by remember { mutableStateOf(firebaseCloud?.currentUserId != null) }
            var cloudBusy by remember { mutableStateOf(false) }
            var cloudStatus by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(firebaseCloud) {
                if (firebaseCloud?.currentUserId != null) {
                    cloudSignedIn = true
                    FirebaseSyncScheduler.enqueue(applicationContext)
                    val sync = runCatching { firebaseCloud.synchronizePendingQuotes() }.getOrNull()
                    if (sync != null) cloudStatus = "Sincronizadas: ${sync.uploaded}; pendientes con error: ${sync.failed}."
                }
            }

            // Definición correcta del estado mutable opcional con Kotlin Compose
            var cotizacionGenerada: CotizacionEntity? by remember { mutableStateOf(null) }

            val listaServicios by viewModel.listaServiciosBaseDatos.collectAsState()
            val listaCotizaciones by viewModel.listaCotizaciones.collectAsState()
            val listaStock by viewModel.listaStock.collectAsState()
            val movimientosStock by viewModel.movimientosStock.collectAsState()
            val tarifasActivas by viewModel.tarifasActivas.collectAsState()
            val historialTarifas by viewModel.historialTarifas.collectAsState()
            val aiInterpretationState by viewModel.aiInterpretationState.collectAsState()
            val screenStateHolder = rememberSaveableStateHolder()
            var csvPendingExport by remember { mutableStateOf("") }
            val exportQuotesLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("text/csv")
            ) { uri ->
                if (uri == null) {
                    Toast.makeText(this@MainActivity, "Exportación cancelada.", Toast.LENGTH_SHORT).show()
                } else {
                    runCatching {
                        contentResolver.openOutputStream(uri)?.use { output ->
                            output.write(csvPendingExport.toByteArray(Charsets.UTF_8))
                        } ?: error("No se pudo crear el archivo.")
                    }.onSuccess {
                        Toast.makeText(this@MainActivity, "CSV exportado sin nombres ni texto del cliente.", Toast.LENGTH_LONG).show()
                    }.onFailure {
                        Toast.makeText(this@MainActivity, "No se pudo guardar el CSV. Inténtalo nuevamente.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            BackHandler(enabled = pantallaActual != DestinoPantalla.INICIO) {
                pantallaActual = when (pantallaActual) {
                    DestinoPantalla.INICIO, DestinoPantalla.LOGIN_ADMIN, DestinoPantalla.PANEL_ADMIN, DestinoPantalla.SELECCION_SERVICIO, DestinoPantalla.ACCESO_NUBE -> DestinoPantalla.INICIO
                    DestinoPantalla.FORMULARIO_SERVICIO -> DestinoPantalla.SELECCION_SERVICIO
                    DestinoPantalla.RESUMEN_PRECIO -> DestinoPantalla.FORMULARIO_SERVICIO
                }
            }
            val reconocedorLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { resultado ->
                processVoiceResult(
                    result = resultado,
                    services = listaServicios,
                    onError = { mensajeVoz = it },
                    onServiceSelected = { serviceId, transcript ->
                        mensajeVoz = null
                        servicioIdSeleccionado = serviceId
                        transcripcionVozInicial = transcript
                        pantallaActual = DestinoPantalla.FORMULARIO_SERVICIO
                    }
                )
            }

            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val stateKey = if (pantallaActual == DestinoPantalla.FORMULARIO_SERVICIO) {
                        "${pantallaActual.name}-$servicioIdSeleccionado"
                    } else pantallaActual.name
                    screenStateHolder.SaveableStateProvider(stateKey) {
                    when (pantallaActual) {
                        DestinoPantalla.INICIO -> {
                            PantallaInicio(
                                versionNombre = BuildConfig.VERSION_NAME,
                                operadorSeleccionado = operadorActual,
                                esModoOffline = esModoOffline,
                                onAlternarModoRed = { esModoOffline = !esModoOffline },
                                onIniciarNuevaCotizacion = {
                                    inicioCotizacionMillis = System.currentTimeMillis()
                                    pantallaActual = DestinoPantalla.SELECCION_SERVICIO
                                },
                                onIrAPanelAdmin = { pantallaActual = DestinoPantalla.LOGIN_ADMIN },
                                cloudConfigured = BuildConfig.FIREBASE_ENABLED,
                                cloudSignedIn = cloudSignedIn,
                                onIrASincronizacion = { pantallaActual = DestinoPantalla.ACCESO_NUBE },
                                cloudStatus = cloudStatus
                            )
                        }

                        DestinoPantalla.LOGIN_ADMIN -> {
                            PantallaLoginAdmin(
                                onAccesoConcedido = { pantallaActual = DestinoPantalla.PANEL_ADMIN },
                                onVolver = { pantallaActual = DestinoPantalla.INICIO }
                            )
                        }

                        DestinoPantalla.ACCESO_NUBE -> {
                            PantallaAccesoNube(
                                signedIn = cloudSignedIn,
                                busy = cloudBusy,
                                statusMessage = cloudStatus,
                                onSignIn = { email, password ->
                                    val cloud = firebaseCloud
                                    if (cloud == null) {
                                        cloudStatus = "Firebase no está configurado para esta compilación."
                                    } else {
                                        cloudBusy = true
                                        cloudStatus = null
                                        lifecycleScope.launch {
                                            runCatching { cloud.signIn(email, password) }
                                                .onSuccess {
                                                    cloudSignedIn = true
                                                    FirebaseSyncScheduler.enqueue(applicationContext)
                                                    val summary = runCatching { cloud.synchronizePendingQuotes() }.getOrNull()
                                                    cloudStatus = if (summary == null) {
                                                        "Cuenta verificada. La sincronización quedó pendiente y se reintentará con conexión."
                                                    } else {
                                                        "Cuenta verificada. Sincronizadas: ${summary.uploaded}; pendientes: ${summary.failed}."
                                                    }
                                                    pantallaActual = DestinoPantalla.INICIO
                                                }
                                                .onFailure {
                                                    cloudStatus = "No se pudo iniciar sesión. Verifica la cuenta, conexión y permiso del responsable."
                                                }
                                            cloudBusy = false
                                        }
                                    }
                                },
                                onSyncNow = {
                                    val cloud = firebaseCloud
                                    if (cloud != null) {
                                        cloudBusy = true
                                        lifecycleScope.launch {
                                            FirebaseSyncScheduler.enqueue(applicationContext)
                                            val summary = runCatching { cloud.synchronizePendingQuotes() }.getOrNull()
                                            cloudStatus = if (summary == null) {
                                                "La sincronización quedó pendiente; se reintentará al recuperar conexión."
                                            } else {
                                                "Sincronizadas: ${summary.uploaded}; pendientes: ${summary.failed}."
                                            }
                                            cloudBusy = false
                                        }
                                    }
                                },
                                onSignOut = {
                                    lifecycleScope.launch {
                                        firebaseCloud?.signOut()
                                        cloudSignedIn = false
                                        cloudStatus = "Sesión cerrada. Las cotizaciones nuevas quedarán solo en este teléfono."
                                    }
                                },
                                onBack = { pantallaActual = DestinoPantalla.INICIO }
                            )
                        }

                        DestinoPantalla.PANEL_ADMIN -> {
                            PantallaPanelAdmin(
                                listaCotizaciones = listaCotizaciones,
                                listaStock = listaStock,
                                servicios = listaServicios,
                                tarifas = historialTarifas,
                                movimientosStock = movimientosStock,
                                onGuardarTarifa = { servicioId, largo, ancho, variante, material, precio ->
                                    viewModel.guardarTarifaAprobada(servicioId, largo, ancho, variante, material, precio) { error ->
                                        Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                                    }
                                },
                                onRegistrarIngreso = { nombre, tipo, cantidad, unidad, medida, motivo, resultado ->
                                    viewModel.registrarIngresoMaterial(nombre, tipo, cantidad, unidad, medida, motivo, operadorActual, resultado)
                                },
                                onRegistrarAjusteStock = { nombre, cambio, unidad, tipo, motivo, resultado ->
                                    viewModel.registrarAjusteStock(nombre, cambio, unidad, tipo, motivo, operadorActual, resultado)
                                },
                                onRegistrarResultado = { cotizacion, resultado ->
                                    viewModel.registrarResultadoComercial(cotizacion, operadorActual, resultado)
                                },
                                onExportarCsv = { quotes ->
                                    csvPendingExport = QuoteCsvExport.build(
                                        quotes,
                                        listaServicios.associate { it.id to it.nombre },
                                        BuildConfig.VERSION_NAME
                                    )
                                    exportQuotesLauncher.launch("cotizaciones_leguia.csv")
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
                                inicioCotizacionMillis = inicioCotizacionMillis,
                                esModoOffline = esModoOffline,
                                transcripcionInicial = transcripcionVozInicial,
                                interpretacionIA = aiInterpretationState.result,
                                interpretacionIALoading = aiInterpretationState.loading,
                                errorIA = aiInterpretationState.error,
                                onInterpretarRequerimiento = viewModel::interpretarRequerimiento,
                                onGuardarFormulario = { cotizacionIngresada ->
                                    val ownerUid = firebaseCloud?.currentUserId
                                    val cloudOwnedQuote = cotizacionIngresada.copy(
                                        firebaseOwnerUid = ownerUid,
                                        firebaseSyncState = if (ownerUid == null) "LOCAL_ONLY" else "PENDING"
                                    )
                                    cotizacionGenerada = applyApprovedPrice(cloudOwnedQuote, tarifasActivas)
                                    pantallaActual = DestinoPantalla.RESUMEN_PRECIO
                                },
                                onVolver = {
                                    viewModel.limpiarInterpretacionIa()
                                    pantallaActual = DestinoPantalla.SELECCION_SERVICIO
                                }
                            )
                        }

                        DestinoPantalla.RESUMEN_PRECIO -> {
                            PantallaResumenPrecio(
                                cotizacion = cotizacionGenerada,
                                servicioNombre = listaServicios.firstOrNull { it.id == cotizacionGenerada?.servicioId }?.nombre.orEmpty(),
                                stock = listaStock,
                                onFinalizarCotizacion = { quote ->
                                    viewModel.registrarCotizacion(quote) {
                                        if (firebaseCloud != null && quote.firebaseOwnerUid != null) {
                                            FirebaseSyncScheduler.enqueue(applicationContext)
                                            lifecycleScope.launch {
                                                val sync = runCatching { firebaseCloud.synchronizePendingQuotes() }.getOrNull()
                                                cloudStatus = if (sync == null) {
                                                    "Cotización guardada en el teléfono; se reintentará la sincronización."
                                                } else {
                                                    "Nube: ${sync.uploaded} sincronizada(s), ${sync.failed} pendiente(s)."
                                                }
                                            }
                                        }
                                        cotizacionGenerada = null
                                        inicioCotizacionMillis = 0L
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
}

private fun processVoiceResult(
    result: ActivityResult,
    services: List<ServicioEntity>,
    onError: (String) -> Unit,
    onServiceSelected: (Int, String) -> Unit
) {
    val transcript = result.data
        ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        ?.firstOrNull()
        ?.trim()
        .orEmpty()
    if (result.resultCode != Activity.RESULT_OK || transcript.isBlank()) {
        onError("No se recibió una transcripción. Puedes intentarlo otra vez o elegir el servicio manualmente.")
        return
    }

    val parsed = SpokenRequirementParser.interpret(transcript)
    val service = parsed.service?.let { name -> services.firstOrNull { it.nombre.equals(name, ignoreCase = true) } }
    if (service == null) {
        val error = if (parsed.ambiguousServices.isNotEmpty()) {
            "Mencionaste más de un servicio (${parsed.ambiguousServices.joinToString()}). Elige uno manualmente."
        } else {
            "No identifiqué Carpas, Toldos o Tapizado. Elige el servicio manualmente."
        }
        onError(error)
        return
    }
    onServiceSelected(service.id, transcript)
}

private fun applyApprovedPrice(
    quote: CotizacionEntity,
    activeRates: List<ParametroPrecioEntity>
): CotizacionEntity {
    val priceKey = quote.medidaLargo?.let { length ->
        quote.medidaAncho?.let { width ->
            runCatching {
                DimensionPriceKey.forSize(length, width, quote.tipoCarpa ?: "BASE", quote.materialDescripcion.orEmpty())
            }.getOrNull()
        }
    }
    val activeRate = activeRates.firstOrNull { it.servicioId == quote.servicioId && it.concepto == priceKey }
        ?: return quote.copy(precioReferencial = 0.0, precioFinal = 0.0, estado = "PENDIENTE_PRECIO")
    val calculation = QuotePricingCalculator.calculate(
        listOf(PriceLine(ConfiguredPrice(activeRate.precioUnitario, activeRate.unidadMedida), 1.0)),
        humanDiscount = 0.0
    )
    return when (calculation) {
        is PricingOutcome.Calculated -> quote.copy(
            precioReferencial = calculation.result.reference,
            precioFinal = calculation.result.final,
            estado = "PENDIENTE"
        )
        else -> quote.copy(precioReferencial = 0.0, precioFinal = 0.0, estado = "PENDIENTE_PRECIO")
    }
}
