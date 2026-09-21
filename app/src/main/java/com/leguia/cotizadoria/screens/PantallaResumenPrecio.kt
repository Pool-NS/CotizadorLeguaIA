package com.leguia.cotizadoria.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leguia.cotizadoria.data.CotizacionRegistro
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaResumenPrecio(
    cotizacion: CotizacionRegistro?,
    onCotizacionFinalizada: (descuento: Double, precioFinal: Double, observaciones: String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var descuentoTexto by remember { mutableStateOf("0") }
    var observacionesTexto by remember { mutableStateOf("") }

    val precioRef = cotizacion?.precioReferencial ?: 0.0
    val descuentoVal = descuentoTexto.toDoubleOrNull() ?: 0.0
    val precioFinal = (precioRef - descuentoVal).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Cotización") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Trazabilidad del Registro", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "ID: ${cotizacion?.id} | Responsable: ${cotizacion?.cotizador}")
                    Text(text = "Servicio: ${cotizacion?.servicio}")
                    Text(text = "Detalles: ${cotizacion?.caracteristicasIngresadas}")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Cálculo Comercial", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Precio Referencial Base:")
                        Text(text = "S/ ${String.format(Locale.US, "%.2f", precioRef)}", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descuentoTexto,
                        onValueChange = { descuentoTexto = it },
                        label = { Text("Descuento Comercial (Decisión del usuario)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "PRECIO FINAL:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "S/ ${String.format(Locale.US, "%.2f", precioFinal)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            OutlinedTextField(
                value = observacionesTexto,
                onValueChange = { observacionesTexto = it },
                label = { Text("Observaciones del proceso") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    Toast.makeText(context, "Cotización confirmada y registrada localmente", Toast.LENGTH_SHORT).show()
                    onCotizacionFinalizada(descuentoVal, precioFinal, observacionesTexto)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Confirmar y Guardar Trazabilidad")
            }

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Corregir datos")
            }
        }
    }
}