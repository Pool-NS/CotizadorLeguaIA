package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leguia.cotizadoria.components.BotonOpcionServicio
import com.leguia.cotizadoria.data.CotizadorOperador
import com.leguia.cotizadoria.data.ServicioEntity
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSeleccionServicio(
    operador: CotizadorOperador,
    serviciosFlow: StateFlow<List<ServicioEntity>>,
    onServicioSeleccionado: (String) -> Unit,
    onVolver: () -> Unit
) {
    val listaServicios by serviciosFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios | Cotiza: ${operador.nombreLegible.split(" ")[0]}") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Selecciona el tipo de trabajo (Room SQLite):", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            if (listaServicios.isEmpty()) {
                CircularProgressIndicator()
            } else {
                listaServicios.forEach { servicio ->
                    BotonOpcionServicio(
                        titulo = servicio.nombre,
                        descripcion = "Cargar parámetros base de ${servicio.nombre}"
                    ) {
                        onServicioSeleccionado(servicio.nombre)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al inicio")
            }
        }
    }
}