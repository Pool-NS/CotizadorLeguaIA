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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.data.StockMaterialRow

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
    var error by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Materiales y stock", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onVolver) { Text("Volver") }
        }
        Spacer(Modifier.height(8.dp))
        Text("Ingresos acumulados por material. Verifica que cada unidad sea consistente.", style = MaterialTheme.typography.bodySmall)
        LazyColumn(Modifier.weight(1f)) {
            if (stock.isEmpty()) item { Text("Aún no hay materiales registrados.", modifier = Modifier.padding(vertical = 12.dp)) }
            items(stock) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.nombreMaterial, style = MaterialTheme.typography.titleMedium)
                        Text("Disponible: ${item.cantidadDisponible} ${item.unidad}")
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Registrar ingreso", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(nombre, { nombre = it; error = null }, label = { Text("Material") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(tipo, { tipo = it }, label = { Text("Tipo o categoría (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(cantidad, { cantidad = it; error = null }, label = { Text("Cantidad que ingresa") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(unidad, { unidad = it; error = null }, label = { Text("Unidad (m, m², unidad, kg...)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
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
