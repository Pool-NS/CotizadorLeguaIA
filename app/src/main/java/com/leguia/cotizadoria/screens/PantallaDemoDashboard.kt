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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.leguia.cotizadoria.data.SyntheticDemoDashboard
import com.leguia.cotizadoria.data.SyntheticDemoQuote
import java.util.Locale

@Composable
fun PantallaDemoDashboard(data: SyntheticDemoDashboard, onVolver: () -> Unit) {
    var showInventory by remember { mutableStateOf(false) }
    var showRates by remember { mutableStateOf(false) }
    var showSamples by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EncabezadoPantalla("Dashboard de prueba", onVolver, subtitulo = "Muestra de 200 registros simulados")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("DEMO · DATOS SINTÉTICOS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                Text("No son ventas, precios ni stock reales de Leguía.", style = MaterialTheme.typography.bodyMedium)
                Text("La factibilidad usa reglas simuladas; no reserva inventario.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Text("Resumen", style = MaterialTheme.typography.titleLarge)
        MetricRow("Cotizaciones", data.quoteCount.toString(), "Conversión simulada", percent(data.conversionRatePercent))
        MetricRow("Tiempo promedio", "${decimal(data.averageMinutes)} min", "Importe promedio", money(data.averagePrice))
        MetricRow("Material promedio", money(data.averageDirectCost), "Aporte directo*", money(data.averageContribution))
        Text("*No incluye mano de obra, gastos indirectos ni impuestos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        GraficoDona(
            titulo = "Resultado de factibilidad",
            valores = data.feasibilityCounts.entries.sortedByDescending { it.value }
                .map { statusLabel(it.key) to it.value }
        )
        GraficoBarras(
            titulo = "Cotizaciones por servicio",
            valores = data.serviceSummary.map { it.service to it.quotes }
        )
        Text("Los indicadores describen solo la simulación; no deben presentarse como resultados observados de la empresa.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        DetailSection("Inventario simulado", "${data.inventory.size} materiales", showInventory) { showInventory = !showInventory }
        if (showInventory) {
            data.inventory.forEach { item ->
                DetailCard(item.name, "${decimal(item.quantity)} ${item.unit} disponibles · costo demo ${money(item.unitCost)} / ${item.unit}")
            }
        }
        DetailSection("Reglas de precio demo", "${data.priceRules.size} reglas", showRates) { showRates = !showRates }
        if (showRates) {
            data.priceRules.forEach { rule ->
                DetailCard(rule.service, "${money(rule.rate)} / ${rule.unit} · ${rule.rule}")
            }
        }
        DetailSection("Escenarios de ejemplo", "${data.sampleQuotes.size} registros", showSamples) { showSamples = !showSamples }
        if (showSamples) data.sampleQuotes.forEach { SampleQuoteCard(it) }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DetailSection(title: String, summary: String, expanded: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onClick) { Text(if (expanded) "Ocultar" else "Ver") }
        }
    }
}

@Composable
private fun DetailCard(title: String, detail: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MetricRow(label1: String, value1: String, label2: String, value2: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard(label1, value1, Modifier.weight(1f))
        MetricCard(label2, value2, Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SampleQuoteCard(quote: SyntheticDemoQuote) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${quote.id} · ${quote.service}", style = MaterialTheme.typography.titleSmall)
            Text("${decimal(quote.length)} × ${decimal(quote.width)} m · ${quote.material}", style = MaterialTheme.typography.bodySmall)
            Text("Precio demo ${money(quote.simulatedPrice)} · material estimado ${money(quote.directCost)}", style = MaterialTheme.typography.bodySmall)
            val result = if (quote.shortage > 0.0) "Faltan ${decimal(quote.shortage)} m²" else "Stock suficiente en este escenario"
            Text(result, color = if (quote.shortage > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            Text(statusLabel(quote.feasibility), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun statusLabel(value: String): String = when (value) {
    "FACTIBLE_CON_REGLAS_SINTETICAS" -> "Factible · regla simulada"
    "FALTA_STOCK" -> "Falta stock · escenario"
    "REVISAR_DATOS" -> "Revisar datos"
    "REVISAR_MARGEN_DIRECTO" -> "Revisar aporte directo"
    else -> value.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
}

private fun money(value: Double): String = String.format(Locale("es", "PE"), "S/ %.2f", value)
private fun decimal(value: Double): String = String.format(Locale("es", "PE"), "%.2f", value)
private fun percent(value: Double): String = String.format(Locale("es", "PE"), "%.1f%%", value)
