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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.StockMaterialRow
import com.leguia.cotizadoria.domain.MaterialReadinessEvaluator
import com.leguia.cotizadoria.domain.MaterialReadinessStatus
import com.leguia.cotizadoria.components.EncabezadoPantalla
import java.util.Locale

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
            Text("No hay datos de cotización disponibles.")
        } else {
            val dimensiones = listOfNotNull(
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
                    Text("Tamaño: ${dimensiones.ifBlank { "Sin medidas" }}")
                    cotizacion.medidaAlto?.let { InfoRow("Altura opcional", "${formatDimension(it)} m") }
                    cotizacion.materialDescripcion?.let { InfoRow("Material", it) }
                    if (cotizacion.medidaAlto != null) {
                        Text("La altura queda registrada, pero las tarifas y el consumo configurados actualmente se basan en largo × ancho. Confirma con el jefe si la altura cambia el precio o el material necesario.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    cotizacion.tipoCarpa?.let { type ->
                        InfoRow("Carpa", if (type == "CERRADA") "Cerrada" else "Abierta")
                        val largestDimension = listOfNotNull(cotizacion.medidaLargo, cotizacion.medidaAncho).maxOrNull()
                        if (largestDimension != null) {
                            InfoRow("Estructura", if (largestDimension >= 4.0) "Tubo galvanizado" else "Tubo electro")
                        }
                        if (type == "CERRADA") {
                            InfoRow("Ventanas", cotizacion.cantidadVentanas?.toString() ?: "—")
                            InfoRow("Puertas", cotizacion.cantidadPuertas?.toString() ?: "—")
                        }
                    }
                    cotizacion.tipoMoto?.let { InfoRow("Tipo de moto", it) }
                }
            }

            if (cotizacion.estado == "PENDIENTE_PRECIO") {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Precio pendiente", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                        Text("No hay un precio aprobado para este servicio y tamaño exacto. El jefe puede configurarlo en Panel de Administración → Tarifas.")
                    }
                }
            } else {
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

            MaterialReadinessCard(cotizacion, servicioNombre, stock)

            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    "Verifica el servicio, el largo, el ancho y el precio antes de guardar.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = { onFinalizarCotizacion(cotizacion) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (cotizacion.estado == "PENDIENTE_PRECIO") "Guardar solicitud pendiente" else "Confirmar y guardar")
            }
        }
    }
}

@Composable
private fun MaterialReadinessCard(cotizacion: CotizacionEntity, serviceName: String, inventory: List<StockMaterialRow>) {
    val readiness = MaterialReadinessEvaluator.assess(
        serviceName = serviceName,
        materialName = cotizacion.materialDescripcion,
        lengthM = cotizacion.medidaLargo,
        widthM = cotizacion.medidaAncho,
        tentType = cotizacion.tipoCarpa,
        inventory = inventory
    )
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Factibilidad de material", style = MaterialTheme.typography.titleMedium)
            when (readiness.status) {
                MaterialReadinessStatus.MATERIAL_MISSING -> Text("Falta indicar el material para revisar inventario.")
                MaterialReadinessStatus.STOCK_NOT_REGISTERED -> Text("${readiness.material} no aparece en el inventario. Requiere revisión del responsable.")
                MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED -> {
                    Text("Stock registrado: ${readiness.available?.let(::decimal) ?: "—"} ${readiness.unit.orEmpty()}.")
                    Text("Falta validar cuánto material consume este servicio y tamaño; no se puede afirmar si alcanza.")
                }
                MaterialReadinessStatus.REQUIREMENT_INCOMPLETE -> Text("Completa largo y ancho para evaluar el escenario sintético.")
                MaterialReadinessStatus.SHORTAGE -> {
                    Text("Faltante estimado: ${decimal(readiness.shortage ?: 0.0)} ${readiness.unit.orEmpty()}", color = MaterialTheme.colorScheme.error)
                    Text("Necesario: ${decimal(readiness.required ?: 0.0)} · disponible: ${decimal(readiness.available ?: 0.0)}")
                }
                MaterialReadinessStatus.SUFFICIENT_SYNTHETIC_SCENARIO -> {
                    Text("Material suficiente en el escenario DEMO: ${decimal(readiness.available ?: 0.0)} ${readiness.unit.orEmpty()} disponibles.", color = MaterialTheme.colorScheme.primary)
                    Text("Consumo estimado de demostración: ${decimal(readiness.required ?: 0.0)} ${readiness.unit.orEmpty()}.")
                }
            }
            if (readiness.synthetic) {
                Text("Valores ficticios de demostración. No usar para aceptar pedidos, comprar ni descontar stock.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            } else {
                Text("La IA interpreta la solicitud; la disponibilidad depende de stock y consumos validados por el negocio. Esta revisión no reserva existencias.", style = MaterialTheme.typography.bodySmall)
            }
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

private fun money(value: Double): String = String.format(Locale("es", "PE"), "S/ %.2f", value)
private fun formatDimension(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
private fun decimal(value: Double): String = String.format(Locale("es", "PE"), "%.2f", value)
