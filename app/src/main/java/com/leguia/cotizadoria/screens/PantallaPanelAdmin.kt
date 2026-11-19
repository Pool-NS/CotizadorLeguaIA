package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.components.EncabezadoPantalla
import com.leguia.cotizadoria.components.GraficoBarras
import com.leguia.cotizadoria.components.GraficoDona
import com.leguia.cotizadoria.components.GraficoTendenciaSemanal
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.ParametroPrecioEntity
import com.leguia.cotizadoria.data.ServicioEntity
import com.leguia.cotizadoria.data.StockMaterialRow
import com.leguia.cotizadoria.data.StockMovementRow
import com.leguia.cotizadoria.domain.QuoteStatus
import com.leguia.cotizadoria.domain.calculateQuoteDurationMetrics
import com.leguia.cotizadoria.ui.format.formatMoney
import com.leguia.cotizadoria.ui.format.formatPercent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PantallaPanelAdmin(
    listaCotizaciones: List<CotizacionEntity>,
    listaStock: List<StockMaterialRow>,
    servicios: List<ServicioEntity>,
    tarifas: List<ParametroPrecioEntity>,
    movimientosStock: List<StockMovementRow>,
    onGuardarTarifa: (Int, Double, Double, String, String, Double) -> Unit,
    onRegistrarIngreso: (String, String, Double, String, String?, String, (String?) -> Unit) -> Unit,
    onRegistrarAjusteStock: (String, Double, String, String, String, (String?) -> Unit) -> Unit,
    onRegistrarResultado: (CotizacionEntity, QuoteStatus) -> Unit,
    onExportarCsv: (List<CotizacionEntity>) -> Unit,
    onVolver: () -> Unit
) {
    var mostrarInventario by remember { mutableStateOf(false) }
    var mostrarTarifas by remember { mutableStateOf(false) }
    var mostrarTodo by remember { mutableStateOf(false) }
    var rangoSeleccionado by remember { mutableStateOf("30 días") }
    BackHandler(enabled = mostrarTarifas || mostrarInventario) {
        if (mostrarTarifas) mostrarTarifas = false else mostrarInventario = false
    }
    if (mostrarTarifas) {
        PantallaTarifas(servicios, tarifas, onGuardarTarifa, onVolver = { mostrarTarifas = false })
        return
    }
    if (mostrarInventario) {
        PantallaInventario(
            stock = listaStock,
            movimientos = movimientosStock,
            onRegistrarIngreso = onRegistrarIngreso,
            onRegistrarAjuste = onRegistrarAjusteStock,
            onVolver = { mostrarInventario = false }
        )
        return
    }

    val cutoff = when (rangoSeleccionado) {
        "7 días" -> System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        "30 días" -> System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        else -> null
    }
    val cotizacionesFiltradas = listaCotizaciones.filter { quote -> cutoff == null || quote.fechaHoraInicio >= cutoff }
    val pendingCount = cotizacionesFiltradas.count { it.estado == QuoteStatus.PENDIENTE.name || it.estado == "PENDIENTE_PRECIO" }
    val completedCount = cotizacionesFiltradas.count { it.estado == QuoteStatus.CONCRETADA.name }
    val notCompletedCount = cotizacionesFiltradas.count { it.estado == QuoteStatus.NO_CONCRETADA.name }
    val resolvedCount = completedCount + notCompletedCount
    val conversionPercent = if (resolvedCount == 0) 0.0 else completedCount * 100.0 / resolvedCount
    val quotedValue = cotizacionesFiltradas.filter { it.estado != "PENDIENTE_PRECIO" }.sumOf { it.precioFinal }
    val communicationEvaluated = cotizacionesFiltradas.filter { it.capturaComunicacionCompleta && it.estado != "PENDIENTE_PRECIO" }
    val communicationPercent = if (communicationEvaluated.isEmpty()) null else
        communicationEvaluated.count { it.fechaHoraPrecioComunicado != null } * 100.0 / communicationEvaluated.size
    val durationMetrics = calculateQuoteDurationMetrics(cotizacionesFiltradas)
    val recentLimit = if (mostrarTodo) cotizacionesFiltradas.size else 8
    val weekly = weeklyQuoteCounts(cotizacionesFiltradas)
    val serviceCounts = servicios.map { service ->
        service.nombre to cotizacionesFiltradas.count { it.servicioId == service.id }
    }.filter { it.second > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { EncabezadoPantalla("Panel del jefe", onVolver, subtitulo = "Resumen de este teléfono") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { mostrarInventario = true }, modifier = Modifier.weight(1f)) { Text("Stock") }
                OutlinedButton(onClick = { mostrarTarifas = true }, modifier = Modifier.weight(1f)) { Text("Tarifas") }
            }
        }
        item { Text("Actividad real guardada localmente", style = MaterialTheme.typography.titleMedium) }
        item {
            Text("Periodo del panel", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("7 días", "30 días", "Todo").forEach { option ->
                    FilterChip(selected = rangoSeleccionado == option, onClick = { rangoSeleccionado = option }, label = { Text(option) })
                }
            }
        }
        if (cotizacionesFiltradas.isNotEmpty()) item {
            OutlinedButton(onClick = { onExportarCsv(cotizacionesFiltradas) }, modifier = Modifier.fillMaxWidth()) {
                Text("Exportar periodo seleccionado · CSV anonimizado")
            }
            Text("No incluye cliente, solicitud ni operador. Fase pre/post, exactitud y errores quedan vacíos para completar solo con el protocolo aprobado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Solicitudes", cotizacionesFiltradas.size.toString(), Modifier.weight(1f))
                MetricCard("Pendientes", pendingCount.toString(), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Concretadas", completedCount.toString(), Modifier.weight(1f))
                MetricCard("Conversión", formatPercent(conversionPercent), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Tiempo promedio en app", durationMetrics.averageMillis?.let(::formatDuration) ?: "—", Modifier.weight(1f))
                MetricCard("Tiempo mediano en app", durationMetrics.medianMillis?.let(::formatDuration) ?: "—", Modifier.weight(1f))
            }
            Text(
                "Tiempo = inicio hasta confirmación del operador de que comunicó el precio; n = ${durationMetrics.sampleCount}. Promedio = suma ÷ n; mediana = valor central o promedio de los dos valores centrales.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard("Precio comunicado", communicationPercent?.let(::formatPercent) ?: "—", Modifier.weight(1f))
                MetricCard("Exactitud auditada", "Sin datos", Modifier.weight(1f))
            }
            Text(
                "Oportunidad confirmada por el operador: ${communicationEvaluated.count { it.fechaHoraPrecioComunicado != null }}/${communicationEvaluated.size}. Fórmula = confirmaciones positivas ÷ capturas completas. Requiere cotejar el instrumento aprobado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Exactitud = cotizaciones correctas verificadas ÷ cotizaciones evaluadas. Falta registrar la auditoría independiente; no se infiere de ventas ni de correcciones de IA.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item { MetricCard("Valor final registrado", formatMoney(quotedValue), Modifier.fillMaxWidth()) }
        item {
            Text("Estos datos pertenecen a este dispositivo. Sin una base de datos central, los teléfonos no comparten el panel.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (cotizacionesFiltradas.isNotEmpty()) {
            item { GraficoTendenciaSemanal("Cotizaciones · últimos 7 días", weekly.map { it.first }, weekly.map { it.second }) }
            item {
                GraficoDona(
                    titulo = "Estado de las solicitudes",
                    valores = listOf("Pendientes" to pendingCount, "Concretadas" to completedCount, "No concretadas" to notCompletedCount)
                )
            }
            if (serviceCounts.isNotEmpty()) item { GraficoBarras("Solicitudes por servicio", serviceCounts) }
        }
        item {
            Text("Conversión = concretadas ÷ (concretadas + no concretadas). Las pendientes se excluyen.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Historial", style = MaterialTheme.typography.titleMedium)
                if (cotizacionesFiltradas.size > 8) TextButton(onClick = { mostrarTodo = !mostrarTodo }) {
                    Text(if (mostrarTodo) "Mostrar menos" else "Ver todas (${cotizacionesFiltradas.size})")
                }
            }
        }
        items(cotizacionesFiltradas.take(recentLimit), key = { it.id }) { cotizacion ->
            TarjetaCotizacionItem(cotizacion = cotizacion, onRegistrarResultado = onRegistrarResultado)
        }
        if (cotizacionesFiltradas.isEmpty()) item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Sin registros para mostrar", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (listaCotizaciones.isEmpty()) "Todavía no hay cotizaciones guardadas en este teléfono." else "Cambia el periodo para consultar otros registros.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private fun weeklyQuoteCounts(quotes: List<CotizacionEntity>): List<Pair<String, Int>> {
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)
    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    val weekday = SimpleDateFormat("EEEEE", Locale.forLanguageTag("es-PE"))
    return (6 downTo 0).map { offset ->
        val date = today.clone() as Calendar
        date.add(Calendar.DAY_OF_YEAR, -offset)
        val label = weekday.format(date.time)
        val key = dateKey.format(date.time)
        val count = quotes.count { quote ->
            runCatching { dateKey.format(Date(quote.fechaHoraInicio)) == key }.getOrDefault(false)
        }
        label to count
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun TarjetaCotizacionItem(cotizacion: CotizacionEntity, onRegistrarResultado: (CotizacionEntity, QuoteStatus) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cotizacion.codigoCotizacion, style = MaterialTheme.typography.titleSmall)
                Text(cotizacion.estado.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase), style = MaterialTheme.typography.labelMedium)
            }
            Text("${cotizacion.operador} · ${cotizacion.requerimientoCliente}", style = MaterialTheme.typography.bodySmall, maxLines = 2)
            if (cotizacion.estado == "PENDIENTE_PRECIO") {
                Text("Pendiente de tarifa aprobada", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            } else {
                Text("Total: ${formatMoney(cotizacion.precioFinal)}", style = MaterialTheme.typography.bodyMedium)
            }
            if (cotizacion.estado == QuoteStatus.PENDIENTE.name && cotizacion.precioReferencial > 0.0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onRegistrarResultado(cotizacion, QuoteStatus.CONCRETADA) }, modifier = Modifier.weight(1f)) { Text("Concretada") }
                    OutlinedButton(onClick = { onRegistrarResultado(cotizacion, QuoteStatus.NO_CONCRETADA) }, modifier = Modifier.weight(1f)) { Text("No concretada") }
                }
            }
        }
    }
}
