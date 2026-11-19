package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.StockMaterialRow
import com.leguia.cotizadoria.domain.MaterialReadinessEvaluator
import com.leguia.cotizadoria.domain.MaterialReadinessStatus
import com.leguia.cotizadoria.components.EncabezadoPantalla
import com.leguia.cotizadoria.ui.format.formatMoney
import com.leguia.cotizadoria.ui.format.formatDecimal

@Composable
fun PantallaResumenPrecio(
    cotizacion: CotizacionEntity?,
    servicioNombre: String,
    stock: List<StockMaterialRow>,
    onFinalizarCotizacion: (CotizacionEntity) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EncabezadoPantalla("Resumen de cotización", onVolver, subtitulo = "Revisa antes de guardar")
        if (cotizacion == null) {
            EmptyQuoteSummary()
        } else {
            QuoteDetailsCard(cotizacion, servicioNombre)
            QuotePriceCard(cotizacion)
            MaterialReadinessCard(cotizacion, stock)
            QuoteSaveAction(cotizacion, onFinalizarCotizacion)
        }
    }
}

@Composable
private fun EmptyQuoteSummary() {
    Text("No hay datos de cotización disponibles.")
}

@Composable
private fun QuoteDetailsCard(cotizacion: CotizacionEntity, servicioNombre: String) {
    val dimensions = listOfNotNull(
        cotizacion.medidaLargo?.let(::formatDimension),
        cotizacion.medidaAncho?.let(::formatDimension)
    ).joinToString(" × ")
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(servicioNombre.ifBlank { "Servicio" }, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            InfoRow("Código", cotizacion.codigoCotizacion)
            InfoRow("Atendió", cotizacion.operador)
            HorizontalDivider()
            Text("Solicitud", style = MaterialTheme.typography.titleMedium)
            Text(cotizacion.requerimientoCliente, style = MaterialTheme.typography.bodyLarge)
            Text("Tamaño: ${dimensions.ifBlank { "Sin medidas" }}")
            cotizacion.medidaAlto?.let { InfoRow("Altura opcional", "${formatDimension(it)} m") }
            cotizacion.materialDescripcion?.let { InfoRow("Material", it) }
            if (cotizacion.medidaAlto != null) HeightPricingNotice()
            cotizacion.tipoCarpa?.let { CarpaDetails(cotizacion, it) }
            cotizacion.tipoMoto?.let { InfoRow("Tipo de moto", it) }
        }
    }
}

@Composable
private fun HeightPricingNotice() {
    Text(
        "La altura queda registrada, pero las tarifas y el consumo configurados actualmente se basan en largo × ancho. Confirma con el jefe si la altura cambia el precio o el material necesario.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun CarpaDetails(cotizacion: CotizacionEntity, type: String) {
    InfoRow("Carpa", if (type == "CERRADA") "Cerrada" else "Abierta")
    val largestDimension = listOfNotNull(cotizacion.medidaLargo, cotizacion.medidaAncho).maxOrNull()
    largestDimension?.let {
        InfoRow("Estructura", if (it >= 4.0) "Tubo galvanizado" else "Tubo electro")
    }
    if (type == "CERRADA") {
        InfoRow("Ventanas", cotizacion.cantidadVentanas?.toString() ?: "—")
        InfoRow("Puertas", cotizacion.cantidadPuertas?.toString() ?: "—")
    }
}

@Composable
private fun QuotePriceCard(cotizacion: CotizacionEntity) {
    if (cotizacion.estado == "PENDIENTE_PRECIO") {
        PendingPriceCard()
    } else {
        ApprovedPriceCard(cotizacion)
    }
}

@Composable
private fun PendingPriceCard() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Precio pendiente", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
            Text("No hay un precio aprobado para este servicio y tamaño exacto. El jefe puede configurarlo en Panel de Administración → Tarifas.")
        }
    }
}

@Composable
private fun ApprovedPriceCard(cotizacion: CotizacionEntity) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Detalle del precio", style = MaterialTheme.typography.titleLarge)
            InfoRow("Tarifa aprobada", money(cotizacion.precioReferencial))
            if (cotizacion.descuentoMonto > 0.0) InfoRow("Descuento", "− ${money(cotizacion.descuentoMonto)}")
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TOTAL", style = MaterialTheme.typography.titleMedium)
                Text(money(cotizacion.precioFinal), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun QuoteSaveAction(cotizacion: CotizacionEntity, onSave: (CotizacionEntity) -> Unit) {
    var priceCommunicated by rememberSaveable(cotizacion.codigoCotizacion) { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
        Text(
            "Verifica el servicio, el largo, el ancho y el precio antes de guardar.",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    if (cotizacion.estado != "PENDIENTE_PRECIO") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.Checkbox(checked = priceCommunicated, onCheckedChange = { priceCommunicated = it })
            Text("Confirmo que comuniqué este precio al cliente.", style = MaterialTheme.typography.bodyMedium)
        }
        Text("Si aún no lo comunicaste, guarda sin marcar la casilla. El panel contará esa respuesta como pendiente de comunicación.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Button(onClick = {
        onSave(
            cotizacion.copy(
                fechaHoraPrecioComunicado = System.currentTimeMillis().takeIf { priceCommunicated },
                capturaComunicacionCompleta = cotizacion.estado != "PENDIENTE_PRECIO"
            )
        )
    }, modifier = Modifier.fillMaxWidth()) {
        Text(when {
            cotizacion.estado == "PENDIENTE_PRECIO" -> "Guardar solicitud pendiente"
            priceCommunicated -> "Confirmar comunicación y guardar"
            else -> "Guardar sin confirmar comunicación"
        })
    }
}

@Composable
private fun MaterialReadinessCard(cotizacion: CotizacionEntity, inventory: List<StockMaterialRow>) {
    val readiness = MaterialReadinessEvaluator.assess(
        materialName = cotizacion.materialDescripcion,
        lengthM = cotizacion.medidaLargo,
        widthM = cotizacion.medidaAncho,
        inventory = inventory
    )
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Factibilidad de material", style = MaterialTheme.typography.titleMedium)
            when (readiness.status) {
                MaterialReadinessStatus.MATERIAL_MISSING -> Text("Falta indicar el material para revisar inventario.")
                MaterialReadinessStatus.STOCK_NOT_REGISTERED -> Text("${readiness.material} no aparece en el inventario. Requiere revisión del responsable.")
                MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED -> {
                    Text("Stock registrado: ${readiness.available?.let(::formatDecimal) ?: "—"} ${readiness.unit.orEmpty()}.")
                    Text("Falta validar cuánto material consume este servicio y tamaño; no se puede afirmar si alcanza.")
                }
                MaterialReadinessStatus.REQUIREMENT_INCOMPLETE -> Text("Completa el largo y el ancho para revisar el material.")
            }
            Text("El stock mostrado es el saldo registrado. Para confirmar si alcanza, el jefe debe validar el consumo por material y medida. Esta revisión no descuenta ni reserva existencias.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun formatDimension(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
private fun money(value: Double): String = formatMoney(value)
