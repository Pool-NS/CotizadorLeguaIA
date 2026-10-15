package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.data.StockMaterialRow
import com.leguia.cotizadoria.components.EncabezadoPantalla

@Composable
fun PantallaInventario(
    stock: List<StockMaterialRow>,
    onRegistrarIngreso: (String, String, Double, String, (String?) -> Unit) -> Unit,
    onVolver: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        EncabezadoPantalla("Materiales y stock", onVolver, subtitulo = "Inventario local")
        Spacer(Modifier.height(8.dp))
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Materiales registrados", style = MaterialTheme.typography.bodySmall)
                    Text(stock.size.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text("Control", style = MaterialTheme.typography.bodySmall)
                    Text("Existencias", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar material") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        Text("Las cantidades respetan su unidad. Aún no hay mínimos de reposición configurados.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val stockVisible = stock.filter { it.nombreMaterial.contains(busqueda, ignoreCase = true) }
        LazyColumn(Modifier.weight(1f)) {
            if (stockVisible.isEmpty()) item {
                Text(
                    if (stock.isEmpty()) "Aún no hay materiales. Registra el primer ingreso abajo." else "No hay resultados para esa búsqueda.",
                    modifier = Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyMedium
                )
            }
            items(stockVisible) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                        Text(item.nombreMaterial, style = MaterialTheme.typography.titleMedium)
                        Text("Existencia registrada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${item.cantidadDisponible} ${item.unidad}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Registrar ingreso", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(nombre, { nombre = it; error = null }, label = { Text("Material") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(tipo, { tipo = it }, label = { Text("Tipo o categoría (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    cantidad, { cantidad = it; error = null }, label = { Text("Cantidad que ingresa") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Text("Unidad de medida", style = MaterialTheme.typography.labelLarge)
                val unidadesFrecuentes = listOf("m", "m²", "kg", "rollo", "unidad")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(unidadesFrecuentes) { opcion ->
                        FilterChip(selected = unidad == opcion, onClick = { unidad = opcion; error = null }, label = { Text(opcion) })
                    }
                    item {
                        FilterChip(selected = unidad.isNotBlank() && unidad !in unidadesFrecuentes, onClick = { unidad = "" }, label = { Text("Otra") })
                    }
                }
                if (unidad.isBlank() || unidad !in unidadesFrecuentes) {
                    OutlinedTextField(unidad, { unidad = it; error = null }, label = { Text("Especifica otra unidad") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                aviso?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    val amount = cantidad.replace(',', '.').toDoubleOrNull()
                    when {
                        nombre.isBlank() || unidad.isBlank() -> error = "Completa el nombre del material y su unidad."
                        amount == null || !amount.isFinite() || amount <= 0.0 -> error = "La cantidad debe ser un número mayor que cero."
                        else -> {
                            onRegistrarIngreso(nombre.trim(), tipo.trim(), amount, unidad.trim()) { result ->
                                if (result == null) {
                                    aviso = "Ingreso registrado. Revisa la lista actualizada."
                                    nombre = ""; tipo = ""; cantidad = ""; unidad = ""; error = null
                                } else error = result
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Registrar ingreso de stock") }
            }
        }
    }
}
