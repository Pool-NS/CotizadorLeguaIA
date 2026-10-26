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
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        EncabezadoPantalla("Precios por medida", onVolver, subtitulo = "Tarifas aprobadas")
        Text("Registra el precio aprobado para cada tamaño. Ejemplo: 3 × 2 y 2 × 2 pueden tener valores diferentes.", style = MaterialTheme.typography.bodySmall)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn(Modifier.weight(1f)) {
            items(servicios) { servicio ->
                TariffServiceCard(
                    service = servicio,
                    rates = tarifas.filter { it.servicioId == servicio.id && it.concepto.startsWith("MEDIDA:") }.sortedBy { it.concepto },
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
                        onGuardar(servicio.id, length, width, variant, material, price)
                        lengths.remove(servicio.id)
                        widths.remove(servicio.id)
                        prices.remove(servicio.id)
                        materials.remove(servicio.id)
                        error = null
                    }
                )
            }
        }
    }
}

@Composable
private fun TariffServiceCard(
    service: ServicioEntity,
    rates: List<ParametroPrecioEntity>,
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
            ApprovedRatesList(service, rates)
            if (servicioEsCarpas(service.nombre)) {
                TentVariantSelector(selectedVariant, onVariantChange)
            }
            TariffRateEditor(
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
private fun ApprovedRatesList(service: ServicioEntity, rates: List<ParametroPrecioEntity>) {
    if (rates.isEmpty()) Text("No hay tamaños con precio aprobado.")
    rates.forEach { rate ->
        val parts = rate.concepto.removePrefix("MEDIDA:").split(':', limit = 3)
        val isLegacy = parts.size < 3
        val variant = parts.getOrNull(0).orEmpty()
        val material = if (isLegacy) "Material sin definir" else parts.getOrNull(1).orEmpty()
        val size = (if (isLegacy) parts.getOrNull(1) else parts.getOrNull(2)).orEmpty().replace('x', '×')
        val label = listOfNotNull(variant.takeIf { servicioEsCarpas(service.nombre) && it != "BASE" }, material, size)
            .joinToString(" · ")
        Text("$label — S/ ${rate.precioUnitario}", style = MaterialTheme.typography.bodyLarge)
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(length, onLengthChange, label = { Text("Largo") }, modifier = Modifier.weight(1f), singleLine = true)
        OutlinedTextField(width, onWidthChange, label = { Text("Ancho") }, modifier = Modifier.weight(1f), singleLine = true)
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
        onClick = { submitRate(service, variant, length, width, material, price, onError, onGuardar) },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Guardar precio de este tamaño") }
}

private fun submitRate(
    service: ServicioEntity,
    selectedVariant: String,
    lengthText: String,
    widthText: String,
    materialText: String,
    priceText: String,
    onError: (String) -> Unit,
    onGuardar: (Double, Double, String, String, Double) -> Unit
) {
    val input = TariffInput(lengthText.toDecimal(), widthText.toDecimal(), materialText.trim(), priceText.toDecimal())
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
