package com.leguia.cotizadoria.screens


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leguia.cotizadoria.data.CotizacionRegistro
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPanelAdmin(
    listaCotizaciones: List<CotizacionRegistro>,
    onEliminarRegistro: (CotizacionRegistro) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración - Posttest") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    if (listaCotizaciones.isEmpty()) {
                        Toast.makeText(context, "Sin registros para exportar", Toast.LENGTH_SHORT).show()
                    } else {
                        val contenidoCsv = StringBuilder()
                        contenidoCsv.append(CotizacionRegistro.obtenerEncabezadoCsv()).append("\n")
                        listaCotizaciones.forEach { item ->
                            contenidoCsv.append(item.toCsvRow()).append("\n")
                        }

                        Toast.makeText(context, "CSV exportado exitosamente (${listaCotizaciones.size} cotizaciones)", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Exportar datos a CSV (Posttest)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Historial Registrado (${listaCotizaciones.size}):",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (listaCotizaciones.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No hay datos de cotizaciones registradas.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listaCotizaciones) { cotizacion ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = cotizacion.id, fontWeight = FontWeight.Bold)
                                    Text(text = "S/ ${String.format(Locale.US, "%.2f", cotizacion.precioFinal)}", fontWeight = FontWeight.Bold)
                                }
                                Text(text = "Cotizador: ${cotizacion.cotizador} | Servicio: ${cotizacion.servicio}", fontSize = 12.sp)
                                Text(text = "Tiempo de proceso: ${cotizacion.obtenerTiempoTotalSegundos()} seg.", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(text = "Eventos registrados: ${cotizacion.eventos.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                                Spacer(modifier = Modifier.height(4.dp))

                                TextButton(
                                    onClick = { onEliminarRegistro(cotizacion) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Eliminar registro (PIN Admin)", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar Sesión Administración")
            }
        }
    }
}