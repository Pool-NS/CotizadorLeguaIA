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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.components.EncabezadoPantalla
import com.leguia.cotizadoria.data.StockMaterialRow
import com.leguia.cotizadoria.data.StockMovementRow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val materialSuggestions = listOf(
    "Tarflex 200 gramos", "Tarflex 300 gramos", "Lona orillo azul", "Lona orillo rojo",
    "Lino económico", "Lino liviano", "Lino pesado"
)
private val frequentUnits = listOf("m", "m²", "kg", "rollo", "unidad")
private val categories = listOf("Carpas", "Toldos", "Tapizado de moto", "Otro")

@Composable
fun PantallaInventario(
    stock: List<StockMaterialRow>,
    movimientos: List<StockMovementRow>,
    onRegistrarIngreso: (String, String, Double, String, String?, String, (String?) -> Unit) -> Unit,
    onRegistrarAjuste: (String, Double, String, String, String, (String?) -> Unit) -> Unit,
    onVolver: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var entryReason by remember { mutableStateOf("") }
    var measure by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var customUnit by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var showForm by remember { mutableStateOf(stock.isEmpty()) }
    var replenishmentMode by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    var confirmEntry by remember { mutableStateOf(false) }
    var confirmMovement by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf<StockMaterialRow?>(null) }
    var movementType by remember { mutableStateOf("SALIDA") }
    var movementAmount by remember { mutableStateOf("") }
    var movementReason by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { EncabezadoPantalla("Materiales y stock", onVolver, subtitulo = "Inventario de este dispositivo") }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Cómo registrar existencias", style = MaterialTheme.typography.titleMedium)
                    Text("Material → presentación → cantidad y unidad.", style = MaterialTheme.typography.bodyMedium)
                    Text("Registra una salida cuando se use o venda material. Reponer suma la nueva cantidad; no se descuenta automáticamente al cotizar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Materiales con saldo", style = MaterialTheme.typography.bodySmall)
                        Text(stock.size.toString(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Los datos se guardan\nen este teléfono", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            if (showForm) {
                OutlinedButton(onClick = { showForm = false }, modifier = Modifier.fillMaxWidth()) { Text("Ocultar formulario") }
            } else {
                Button(onClick = { showForm = true; replenishmentMode = false; notice = null }, modifier = Modifier.fillMaxWidth()) { Text("＋ Agregar material") }
            }
        }
        if (showForm) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (replenishmentMode) "Reponer material" else "Nuevo ingreso", style = MaterialTheme.typography.titleLarge)
                        Text("La cantidad se sumará al saldo actual. Registra el ingreso real y su presentación.", style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it.take(60); error = null },
                            label = { Text("Nombre exacto del material") },
                            placeholder = { Text("Ej.: Tarflex 200 gramos") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Text("Sugerencias de nombre", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(materialSuggestions) { suggestion ->
                                FilterChip(selected = name == suggestion, onClick = { name = suggestion; error = null }, label = { Text(suggestion) })
                            }
                        }
                        Text("Categoría", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { option ->
                                FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option) })
                            }
                        }
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { value -> if (value.length <= 14) { quantity = value; error = null } },
                            label = { Text("Cantidad contada") },
                            placeholder = { Text("Ej.: 25.5") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        OutlinedTextField(
                            value = measure,
                            onValueChange = { measure = it.take(60); error = null },
                            label = { Text("Medida o presentación (opcional)") },
                            placeholder = { Text("Ej.: ancho 1.50 m; rollo de 50 m; pieza 3 × 2 m") },
                            supportingText = { Text("Describe el tamaño del material. No es la cantidad disponible.") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        OutlinedTextField(
                            value = entryReason,
                            onValueChange = { entryReason = it.take(160); error = null },
                            label = { Text("Motivo o referencia del ingreso") },
                            placeholder = { Text("Ej.: compra, reposición, conteo inicial") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        Text("Unidad de esa cantidad", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(frequentUnits) { option ->
                                FilterChip(selected = unit == option, onClick = { unit = option; error = null }, label = { Text(option) })
                            }
                            item {
                                FilterChip(selected = unit == "otra", onClick = { unit = "otra"; customUnit = ""; error = null }, label = { Text("Otra") })
                            }
                        }
                        if (unit == "otra") {
                            OutlinedTextField(
                                value = customUnit, onValueChange = { value -> customUnit = value.take(20); error = null },
                                label = { Text("Escribe la unidad") }, placeholder = { Text("Ej.: paquete") },
                                modifier = Modifier.fillMaxWidth(), singleLine = true
                            )
                        }
                        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        notice?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                        Button(onClick = {
                            val parsedQuantity = quantity.trim().replace(',', '.').toDoubleOrNull()
                            val selectedUnit = if (unit == "otra") customUnit.trim() else unit.trim()
                            when {
                                name.isBlank() -> error = "Escribe o elige el nombre del material."
                                entryReason.isBlank() -> error = "Indica el motivo o referencia del ingreso."
                                selectedUnit.isBlank() -> error = "Elige una unidad o escribe una unidad válida."
                                parsedQuantity == null || !parsedQuantity.isFinite() || parsedQuantity <= 0.0 -> error = "La cantidad debe ser un número mayor que cero."
                                parsedQuantity > 1_000_000_000.0 -> error = "Revisa la cantidad; supera el límite permitido."
                                else -> confirmEntry = true
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Guardar ingreso de stock") }
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = search, onValueChange = { search = it.take(60) },
                label = { Text("Buscar material registrado") }, modifier = Modifier.fillMaxWidth(), singleLine = true
            )
        }
        item {
            val availableCategories = listOf("Todas") + stock.mapNotNull { it.categoria?.takeIf(String::isNotBlank) }.distinct().sorted()
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(availableCategories) { option ->
                    FilterChip(
                        selected = selectedCategory == option,
                        onClick = { selectedCategory = option },
                        label = { Text(option) }
                    )
                }
            }
        }
        val visibleStock = stock.filter { item ->
            item.nombreMaterial.contains(search, ignoreCase = true) &&
                (selectedCategory == "Todas" || item.categoria.equals(selectedCategory, ignoreCase = true))
        }
        if (visibleStock.isEmpty()) item {
            Text(
                if (stock.isEmpty()) "Aún no hay stock real. Registra el primer ingreso arriba." else "No encontramos materiales con ese nombre.",
                modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium
            )
        }
        items(visibleStock, key = { it.nombreMaterial }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.nombreMaterial, style = MaterialTheme.typography.titleMedium)
                        item.categoria?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        item.medidaPresentacion?.let { Text("Medida: $it", style = MaterialTheme.typography.bodySmall) }
                        Text("Saldo registrado · requiere regla de consumo para validar suficiencia", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("${item.cantidadDisponible} ${item.unidad}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    name = item.nombreMaterial
                    category = item.categoria.orEmpty()
                    measure = item.medidaPresentacion.orEmpty()
                    quantity = ""
                    entryReason = "Reposición por compra o ingreso"
                    customUnit = ""
                    unit = if (item.unidad in frequentUnits) item.unidad else "otra"
                    if (unit == "otra") customUnit = item.unidad
                    selectedMaterial = null
                    movementAmount = ""
                    movementReason = ""
                    notice = null
                    replenishmentMode = true
                    error = null
                    showForm = true
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                }, modifier = Modifier.weight(1f)) { Text("＋ Reponer") }
                OutlinedButton(onClick = {
                    selectedMaterial = item
                    movementAmount = ""
                    movementReason = ""
                    error = null
                }, modifier = Modifier.weight(1f)) { Text("Salida / ajuste") }
                }
            }
        }
        if (selectedMaterial != null) item {
            val selected = selectedMaterial!!
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Movimiento · ${selected.nombreMaterial}", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("SALIDA", "AJUSTE").forEach { type ->
                            FilterChip(selected = movementType == type, onClick = { movementType = type }, label = { Text(if (type == "SALIDA") "Salida" else "Ajuste ±") })
                        }
                    }
                    OutlinedTextField(
                        value = movementAmount,
                        onValueChange = { movementAmount = it.take(14) },
                        label = { Text(if (movementType == "SALIDA") "Cantidad a retirar (${selected.unidad})" else "Cambio (+ o − ${selected.unidad})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedTextField(
                        value = movementReason, onValueChange = { movementReason = it.take(160) },
                        label = { Text("Motivo obligatorio") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { selectedMaterial = null; error = null }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                        Button(onClick = {
                            val amount = movementAmount.trim().replace(',', '.').toDoubleOrNull()
                            when {
                                amount == null || !amount.isFinite() || amount == 0.0 -> error = "Ingresa un cambio distinto de cero."
                                movementType == "SALIDA" && amount < 0.0 -> error = "Para una salida, ingresa la cantidad como número positivo."
                                movementReason.isBlank() -> error = "Escribe el motivo del movimiento."
                                else -> confirmMovement = true
                            }
                        }, modifier = Modifier.weight(1f)) { Text("Guardar movimiento") }
                    }
                }
            }
        }
        if (movimientos.isNotEmpty()) item {
            Text("Movimientos recientes", style = MaterialTheme.typography.titleLarge)
            Text("Se muestran hasta 50 movimientos registrados en este teléfono.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(movimientos, key = { "${it.fecha}-${it.nombreMaterial}-${it.tipo}-${it.cantidadCambio}" }) { movement ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("${movement.tipo}: ${movement.nombreMaterial}", style = MaterialTheme.typography.titleSmall)
                    Text("${if (movement.cantidadCambio > 0) "+" else ""}${movement.cantidadCambio} ${movement.unidad} · ${movement.responsable}", style = MaterialTheme.typography.bodyMedium)
                    movement.observacion?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    Text(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(movement.fecha)), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
    if (confirmEntry) {
        val parsedQuantity = quantity.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
        val selectedUnit = if (unit == "otra") customUnit.trim() else unit.trim()
        AlertDialog(
            onDismissRequest = { confirmEntry = false },
            title = { Text("Confirmar ingreso") },
            text = { Text("Se registrarán $parsedQuantity $selectedUnit de $name${measure.takeIf(String::isNotBlank)?.let { " · $it" }.orEmpty()}. Motivo: $entryReason") },
            confirmButton = {
                TextButton(onClick = {
                    confirmEntry = false
                    onRegistrarIngreso(name.trim(), category, parsedQuantity, selectedUnit, measure.trim().takeIf(String::isNotBlank), entryReason.trim()) { result ->
                        if (result == null) {
                            notice = "Ingreso guardado. El saldo ya aparece en la lista."
                            replenishmentMode = false
                            name = ""; category = ""; quantity = ""; measure = ""; entryReason = ""; unit = ""; customUnit = ""; error = null
                        } else error = result
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { confirmEntry = false }) { Text("Revisar") } }
        )
    }
    if (confirmMovement) {
        val selected = selectedMaterial
        val amount = movementAmount.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
        if (selected != null) AlertDialog(
            onDismissRequest = { confirmMovement = false },
            title = { Text("Confirmar movimiento") },
            text = { Text("${movementType.lowercase().replaceFirstChar(Char::uppercase)} de $amount ${selected.unidad} para ${selected.nombreMaterial}. Motivo: $movementReason") },
            confirmButton = {
                TextButton(onClick = {
                    confirmMovement = false
                    onRegistrarAjuste(selected.nombreMaterial, amount, selected.unidad, movementType, movementReason.trim()) { result ->
                        if (result == null) {
                            notice = "Movimiento registrado y saldo actualizado."
                            selectedMaterial = null
                            movementAmount = ""
                            movementReason = ""
                            error = null
                        } else error = result
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { confirmMovement = false }) { Text("Revisar") } }
        )
    }
}
