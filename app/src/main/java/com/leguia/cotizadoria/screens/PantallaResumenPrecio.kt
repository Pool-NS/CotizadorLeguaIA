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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.data.CotizacionEntity
import java.util.Locale

@Composable
fun PantallaResumenPrecio(
    cotizacion: CotizacionEntity?,
    servicioNombre: String,
    onFinalizarCotizacion: (CotizacionEntity) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Resumen de cotización", style = MaterialTheme.typography.headlineSmall)
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
                    cotizacion.tipoCarpa?.let { type ->
                        InfoRow("Carpa", if (type == "CERRADA") "Cerrada" else "Abierta")
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

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Materiales y stock", style = MaterialTheme.typography.titleMedium)
                    Text("El inventario se consulta en Administración → Materiales y stock. Esta cotización aún no descuenta materiales porque falta definir el consumo por servicio y tamaño.", style = MaterialTheme.typography.bodyMedium)
                }
            }

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
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver a editar") }
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
