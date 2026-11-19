package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
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
import com.leguia.cotizadoria.components.EncabezadoPantalla

@Composable
fun PantallaTarifas(
    servicios: List<ServicioEntity>,
    tarifas: List<ParametroPrecioEntity>,
    onGuardar: (Int, Double, Double, String, String, Double) -> Unit,
    onVolver: () -> Unit
) {
    val lengths = remember { mutableStateMapOf<Int, String>() }
    val widths = remember { mutableStateMapOf<Int, String>() }
    val prices = remember { mutableStateMapOf<Int, String>() }
    val materials = remember { mutableStateMapOf<Int, String>() }
    val variants = remember { mutableStateMapOf<Int, String>() }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var serviceFilter by remember { mutableStateOf("Todos") }
    var sizeFilter by remember { mutableStateOf("Todos") }
    var editorServiceId by remember { mutableStateOf<Int?>(null) }
    val historyVisibility = remember { mutableStateMapOf<Int, Boolean>() }
    var pendingRate by remember { mutableStateOf<PendingRate?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        EncabezadoPantalla("Tarifas", onVolver, subtitulo = "Consulta y configuración de precios")
        Text("Los precios vigentes se usan al cotizar. Los anteriores quedan en el historial.", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it.take(60) },
            label = { Text("Buscar material o medida") },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(listOf("Todos") + servicios.map { it.nombre }.distinct()) { option ->
                FilterChip(selected = serviceFilter == option, onClick = { serviceFilter = option }, label = { Text(option) })
            }
        }
        val commonSizes = listOf("Todos", "2×2", "2.5×2", "3×2", "3×4", "4×4", "5×4", "6×3", "6×4", "6×5", "6×6", "7×4")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(commonSizes) { option ->
                FilterChip(selected = sizeFilter == option, onClick = { sizeFilter = option }, label = { Text(option) })
            }
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(Modifier.weight(1f)) {
            items(servicios.filter { serviceFilter == "Todos" || it.nombre == serviceFilter }) { servicio ->
                val serviceRates = tarifas.filter { rate ->
                    if (rate.servicioId != servicio.id || !rate.concepto.startsWith("MEDIDA:")) return@filter false
                    val normalizedSize = sizeFilter.replace("×", "x")
                    (sizeFilter == "Todos" || rate.concepto.contains(normalizedSize, ignoreCase = true)) &&
                        (query.isBlank() || rate.concepto.contains(query, ignoreCase = true) || servicio.nombre.contains(query, ignoreCase = true) || rate.precioUnitario.toString().contains(query))
                }.sortedBy { it.concepto }
                TariffServiceCard(
                    service = servicio,
                    rates = serviceRates,
                    historyVisible = historyVisibility[servicio.id] == true,
                    onToggleHistory = { historyVisibility[servicio.id] = historyVisibility[servicio.id] != true },
                    editorVisible = editorServiceId == servicio.id,
                    onToggleEditor = { editorServiceId = if (editorServiceId == servicio.id) null else servicio.id },
                    selectedVariant = variants[servicio.id] ?: "ABIERTA",
                    onVariantChange = { variants[servicio.id] = it },
                    length = lengths[servicio.id].orEmpty(),
                    onLengthChange = { lengths[servicio.id] = it; error = null },
                    width = widths[servicio.id].orEmpty(),
                    onWidthChange = { widths[servicio.id] = it; error = null },
                    material = materials[servicio.id].orEmpty(),
                    onMaterialChange = { materials[servicio.id] = it; error = null },
                    price = prices[servicio.id].orEmpty(),
                    onPriceChange = { prices[servicio.id] = it; error = null },
                    onError = { error = it },
                    onGuardar = { length, width, variant, material, price ->
                        pendingRate = PendingRate(servicio.id, servicio.nombre, length, width, variant, material, price)
                    }
                )
            }
        }
    }
    pendingRate?.let { rate ->
        AlertDialog(
            onDismissRequest = { pendingRate = null },
            title = { Text("Confirmar tarifa") },
            text = { Text("${rate.serviceName} · ${rate.material} · ${rate.length} × ${rate.width} m · S/ ${rate.price}. La versión anterior de esta combinación quedará inactiva y se conservará en el historial.") },
            confirmButton = {
                TextButton(onClick = {
                    onGuardar(rate.serviceId, rate.length, rate.width, rate.variant, rate.material, rate.price)
                    lengths.remove(rate.serviceId)
                    widths.remove(rate.serviceId)
                    prices.remove(rate.serviceId)
                    materials.remove(rate.serviceId)
                    editorServiceId = null
                    pendingRate = null
                    error = null
                }) { Text("Confirmar y guardar") }
            },
            dismissButton = { TextButton(onClick = { pendingRate = null }) { Text("Revisar") } }
        )
    }
}

private data class PendingRate(
    val serviceId: Int,
    val serviceName: String,
    val length: Double,
    val width: Double,
    val variant: String,
    val material: String,
    val price: Double
)

@Composable
private fun TariffServiceCard(
    service: ServicioEntity,
    rates: List<ParametroPrecioEntity>,
    historyVisible: Boolean,
    onToggleHistory: () -> Unit,
    editorVisible: Boolean,
    onToggleEditor: () -> Unit,
    selectedVariant: String,
    onVariantChange: (String) -> Unit,
    length: String,
    onLengthChange: (String) -> Unit,
    width: String,
    onWidthChange: (String) -> Unit,
    material: String,
    onMaterialChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    onError: (String) -> Unit,
    onGuardar: (Double, Double, String, String, Double) -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(service.nombre, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            if (rates.isEmpty()) Text("Sin resultados para estos filtros.", style = MaterialTheme.typography.bodySmall)
            else ApprovedRatesList(service, rates, historyVisible, onToggleHistory)
            OutlinedButton(onClick = onToggleEditor, modifier = Modifier.fillMaxWidth()) {
                Text(if (editorVisible) "Cerrar formulario" else "Agregar o actualizar tarifa")
            }
            if (editorVisible && servicioEsCarpas(service.nombre)) {
                TentVariantSelector(selectedVariant, onVariantChange)
            }
            if (editorVisible) TariffRateEditor(
                service = service,
                variant = selectedVariant,
                length = length,
                onLengthChange = onLengthChange,
                width = width,
                onWidthChange = onWidthChange,
                material = material,
                onMaterialChange = onMaterialChange,
                price = price,
                onPriceChange = onPriceChange,
                onError = onError,
                onGuardar = onGuardar
            )
        }
    }
}

@Composable
private fun ApprovedRatesList(
    service: ServicioEntity,
    rates: List<ParametroPrecioEntity>,
    historyVisible: Boolean,
    onToggleHistory: () -> Unit
) {
    val currentRates = rates.filter { it.activo }
    val historicalRates = rates.filterNot { it.activo }
    if (currentRates.isEmpty()) Text("No hay precios vigentes para esta selección.", style = MaterialTheme.typography.bodySmall)
    currentRates.forEach { rate ->
        RateSummaryCard(service, rate, historical = false)
    }
    if (historicalRates.isNotEmpty()) {
        OutlinedButton(onClick = onToggleHistory, modifier = Modifier.fillMaxWidth()) {
            Text(if (historyVisible) "Ocultar historial (${historicalRates.size})" else "Ver precios anteriores (${historicalRates.size})")
        }
        if (historyVisible) historicalRates.forEach { rate -> RateSummaryCard(service, rate, historical = true) }
    }
}

@Composable
private fun RateSummaryCard(service: ServicioEntity, rate: ParametroPrecioEntity, historical: Boolean) {
        val parts = rate.concepto.removePrefix("MEDIDA:").split(':', limit = 3)
        val isLegacy = parts.size < 3
        val variant = parts.getOrNull(0).orEmpty()
        val material = if (isLegacy) "Material sin definir" else parts.getOrNull(1).orEmpty()
        val size = (if (isLegacy) parts.getOrNull(1) else parts.getOrNull(2)).orEmpty().replace('x', '×')
    Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(size.ifBlank { "Medida sin registrar" }, style = MaterialTheme.typography.titleMedium)
                Text(material, style = MaterialTheme.typography.bodyMedium)
                if (servicioEsCarpas(service.nombre) && variant != "BASE") {
                    Text(if (variant == "CERRADA") "Cerrada" else "Abierta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("S/ ${"%.2f".format(java.util.Locale.ROOT, rate.precioUnitario)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Surface(
                    color = if (historical) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (historical) "Anterior" else "Vigente",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (historical) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TentVariantSelector(selected: String, onSelect: (String) -> Unit) {
    Text("Tipo de carpa", style = MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("ABIERTA" to "Abierta", "CERRADA" to "Cerrada").forEach { (value, label) ->
            if (selected == value) {
                Button(onClick = { onSelect(value) }) { Text(label) }
            } else {
                OutlinedButton(onClick = { onSelect(value) }) { Text(label) }
            }
        }
    }
}

@Composable
private fun TariffRateEditor(
    service: ServicioEntity,
    variant: String,
    length: String,
    onLengthChange: (String) -> Unit,
    width: String,
    onWidthChange: (String) -> Unit,
    material: String,
    onMaterialChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    onError: (String) -> Unit,
    onGuardar: (Double, Double, String, String, Double) -> Unit
) {
    Spacer(Modifier.height(4.dp))
    Text("Medidas frecuentes (m)", style = MaterialTheme.typography.labelLarge)
    val commonSizes = listOf("2×2", "2.5×2", "3×2", "3×4", "4×4", "5×4", "6×3", "6×4", "6×5", "6×6", "7×4")
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(commonSizes) { size ->
            val dimensions = size.split('×')
            FilterChip(
                selected = length == dimensions[0] && width == dimensions[1],
                onClick = {
                    onLengthChange(dimensions[0])
                    onWidthChange(dimensions[1])
                },
                label = { Text(size) }
            )
        }
        item {
            FilterChip(
                selected = false,
                onClick = { onLengthChange(""); onWidthChange("") },
                label = { Text("Medida propia") }
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(length, { value -> if (value.length <= 8) onLengthChange(value) }, label = { Text("Largo (m)") }, modifier = Modifier.weight(1f), singleLine = true)
        OutlinedTextField(width, { value -> if (value.length <= 8) onWidthChange(value) }, label = { Text("Ancho (m)") }, modifier = Modifier.weight(1f), singleLine = true)
    }
    val materialSuggestions = when {
        servicioEsCarpas(service.nombre) -> listOf("Lino económico", "Lino liviano", "Lino pesado")
        service.nombre.equals("Toldos", ignoreCase = true) -> listOf("Tarflex 200 gramos", "Tarflex 300 gramos", "Lona orillo azul", "Lona orillo rojo")
        else -> emptyList()
    }
    if (materialSuggestions.isNotEmpty()) {
        Text("Usa el mismo nombre de material que en la cotización", style = MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(materialSuggestions) { option ->
                FilterChip(selected = material == option, onClick = { onMaterialChange(option) }, label = { Text(option) })
            }
        }
    }
    OutlinedTextField(
        value = material,
        onValueChange = onMaterialChange,
        label = { Text("Material exacto") },
        placeholder = { Text("Ejemplo: lona, tela Oxford") },
        supportingText = { Text("La tarifa se aplicará solo a este material. Usa el mismo nombre en cada cotización.") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    OutlinedTextField(
        value = price,
        onValueChange = onPriceChange,
        label = { Text("Precio aprobado (S/)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Button(
        onClick = {
            submitRate(
                service,
                variant,
                TariffFormInput(length, width, material, price),
                onError,
                onGuardar
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Guardar precio de este tamaño") }
}

private fun submitRate(
    service: ServicioEntity,
    selectedVariant: String,
    form: TariffFormInput,
    onError: (String) -> Unit,
    onGuardar: (Double, Double, String, String, Double) -> Unit
) {
    val input = TariffInput(
        form.length.toDecimal(),
        form.width.toDecimal(),
        form.material.trim(),
        form.price.toDecimal()
    )
    val validationError = input.validationError()
    if (validationError != null) {
        onError(validationError)
        return
    }
    onGuardar(
        input.length!!,
        input.width!!,
        if (servicioEsCarpas(service.nombre)) selectedVariant else "BASE",
        input.material,
        input.price!!
    )
}

private data class TariffFormInput(
    val length: String,
    val width: String,
    val material: String,
    val price: String
)

private data class TariffInput(val length: Double?, val width: Double?, val material: String, val price: Double?) {
    fun validationError(): String? = when {
        length == null || !length.isFinite() || length <= 0.0 -> "Ingresa un largo mayor que cero."
        width == null || !width.isFinite() || width <= 0.0 -> "Ingresa un ancho mayor que cero."
        material.isBlank() -> "Indica el material para asociar el precio aprobado."
        price == null || !price.isFinite() || price <= 0.0 -> "Ingresa el precio aprobado mayor que cero."
        else -> null
    }
}

private fun String.toDecimal(): Double? = replace(',', '.').toDoubleOrNull()

private fun servicioEsCarpas(nombre: String): Boolean = nombre.equals("Carpas", ignoreCase = true)
