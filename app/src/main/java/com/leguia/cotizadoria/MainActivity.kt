package com.leguia.cotizadoria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.leguia.cotizadoria.data.AppDatabase
import com.leguia.cotizadoria.data.CotizadorRepository
import com.leguia.cotizadoria.screens.*
import com.leguia.cotizadoria.viewmodel.CotizadorViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: CotizadorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de Room y Repositorio
        val database = AppDatabase.obtenerBaseDatos(this)
        val repository = CotizadorRepository(database.servicioDao())

        // Inicialización del ViewModel mediante Factory
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CotizadorViewModel(repository) as T
            }
        })[CotizadorViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppCotizadorNav(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppCotizadorNav(viewModel: CotizadorViewModel) {
    when (viewModel.pantallaActual) {
        "INICIO" -> PantallaInicio(
            operadorActual = viewModel.operadorSeleccionado,
            esOffline = viewModel.esModoOffline,
            onCambiarOperador = { viewModel.cambiarOperador(it) },
            onCambiarModoRed = { viewModel.alternarModoRed(it) },
            onNavegarSeleccion = { viewModel.navegarA("SELECCION_SERVICIO") },
            onNavegarAdmin = { viewModel.navegarA("LOGIN_ADMIN") }
        )
        "SELECCION_SERVICIO" -> PantallaSeleccionServicio(
            operador = viewModel.operadorSeleccionado,
            serviciosFlow = viewModel.listaServiciosBaseDatos,
            onServicioSeleccionado = { servicio -> viewModel.iniciarNuevaCotizacion(servicio) },
            onVolver = { viewModel.navegarA("INICIO") }
        )
        "FORMULARIO_DATOS" -> PantallaFormularioServicio(
            servicio = viewModel.servicioSeleccionado,
            cotizacion = viewModel.cotizacionActual,
            esOffline = viewModel.esModoOffline,
            onProcesarIA = { prompt -> viewModel.procesarEntradaIa(prompt) },
            onContinuar = { resumen, manuales -> viewModel.guardarFormulario(resumen, manuales) },
            onVolver = { viewModel.navegarA("SELECCION_SERVICIO") }
        )
        "RESUMEN_PRECIO" -> PantallaResumenPrecio(
            cotizacion = viewModel.cotizacionActual,
            onCotizacionFinalizada = { desc, precioFinal, obs -> viewModel.finalizarCotizacion(desc, precioFinal, obs) },
            onVolver = { viewModel.navegarA("FORMULARIO_DATOS") }
        )
        "LOGIN_ADMIN" -> PantallaLoginAdmin(
            onAccesoConcedido = { viewModel.navegarA("PANEL_ADMIN") },
            onVolver = { viewModel.navegarA("INICIO") }
        )
        "PANEL_ADMIN" -> PantallaPanelAdmin(
            listaCotizaciones = viewModel.listaCotizaciones.toList(),
            onEliminarRegistro = { reg -> viewModel.eliminarCotizacionConPin(reg) },
            onVolver = { viewModel.navegarA("INICIO") }
        )
    }
}