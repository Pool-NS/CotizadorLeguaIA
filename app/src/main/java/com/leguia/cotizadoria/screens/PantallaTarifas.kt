package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.data.ParametroPrecioEntity
import com.leguia.cotizadoria.data.ServicioEntity

@Composable
fun PantallaTarifas(
    servicios: List<ServicioEntity>,
    tarifas: List<ParametroPrecioEntity>,
    onGuardar: (Int, Double, Double, String, Double) -> Unit,
    onVolver: () -> Unit
) {
    val lengths = remember { mutableStateMapOf<Int, String>() }
    val widths = remember { mutableStateMapOf<Int, String>() }
    val prices = remember { mutableStateMapOf<Int, String>() }
    val variants = remember { mutableStateMapOf<Int, String>() }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Precios por medida", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onVolver) { Text("Volver") }
        }
        Text("Registra el precio aprobado para cada tamaño. Ejemplo: 3 × 2 y 2 × 2 pueden tener valores diferentes.", style = MaterialTheme.typography.bodySmall)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(Modifier.weight(1f)) {
            items(servicios) { servicio ->
                val serviceRates = tarifas.filter { it.servicioId == servicio.id && it.concepto.startsWith("MEDIDA:") }
                    .sortedBy { it.concepto }
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(servicio.nombre, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        if (serviceRates.isEmpty()) Text("No hay tamaños con precio aprobado.")
                        serviceRates.forEach { rate ->
                            val keyParts = rate.concepto.removePrefix("MEDIDA:").split(':', limit = 2)
                            val variant = keyParts.getOrNull(0).orEmpty()
                            val size = keyParts.getOrNull(1).orEmpty().replace('x', '×')
                            val label = if (servicio.nombre.equals("Carpas", ignoreCase = true)) "$variant · $size" else size
                            Text("$label — S/ ${rate.precioUnitario}", style = MaterialTheme.typography.bodyLarge)
                        }
                        if (servicioEsCarpas(servicio.nombre)) {
                            Text("Tipo de carpa", style = MaterialTheme.typography.labelLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("ABIERTA" to "Abierta", "CERRADA" to "Cerrada").forEach { (value, label) ->
                                    if ((variants[servicio.id] ?: "ABIERTA") == value) {
                                        Button(onClick = { variants[servicio.id] = value }) { Text(label) }
                                    } else {
                                        OutlinedButton(onClick = { variants[servicio.id] = value }) { Text(label) }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = lengths[servicio.id].orEmpty(),
                                onValueChange = { lengths[servicio.id] = it; error = null },
                                label = { Text("Largo") }, modifier = Modifier.weight(1f), singleLine = true
                            )
                            OutlinedTextField(
                                value = widths[servicio.id].orEmpty(),
                                onValueChange = { widths[servicio.id] = it; error = null },
                                label = { Text("Ancho") }, modifier = Modifier.weight(1f), singleLine = true
                            )
                        }
                        OutlinedTextField(
                            value = prices[servicio.id].orEmpty(),
                            onValueChange = { prices[servicio.id] = it; error = null },
                            label = { Text("Precio aprobado (S/)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Button(onClick = {
                            val length = lengths[servicio.id]?.replace(',', '.')?.toDoubleOrNull()
                            val width = widths[servicio.id]?.replace(',', '.')?.toDoubleOrNull()
                            val price = prices[servicio.id]?.replace(',', '.')?.toDoubleOrNull()
                            when {
                                length == null || !length.isFinite() || length <= 0.0 -> error = "Ingresa un largo mayor que cero."
                                width == null || !width.isFinite() || width <= 0.0 -> error = "Ingresa un ancho mayor que cero."
                                price == null || !price.isFinite() || price <= 0.0 -> error = "Ingresa el precio aprobado mayor que cero."
                                else -> {
                                    val variant = if (servicioEsCarpas(servicio.nombre)) variants[servicio.id] ?: "ABIERTA" else "BASE"
                                    onGuardar(servicio.id, length, width, variant, price)
                                    lengths.remove(servicio.id); widths.remove(servicio.id); prices.remove(servicio.id); error = null
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar precio de este tamaño") }
                    }
                }
            }
        }
    }
}

private fun servicioEsCarpas(nombre: String): Boolean = nombre.equals("Carpas", ignoreCase = true)
