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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.data.SyntheticDemoDashboard
import com.leguia.cotizadoria.data.SyntheticDemoQuote
import java.util.Locale

@Composable
fun PantallaDemoDashboard(data: SyntheticDemoDashboard, onVolver: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dashboard de demostración", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onVolver) { Text("Volver") }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DATOS SINTÉTICOS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                Text("Los 200 registros, tarifas y saldos de stock son simulados. Sirven para probar la app y el análisis; no representan historial ni precios reales de Leguía.")
                Text("La factibilidad se calcula con las reglas ficticias de esta demostración. No reserva inventario ni decide compras.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("Indicadores", style = MaterialTheme.typography.titleLarge)
        MetricRow("Cotizaciones", data.quoteCount.toString(), "Conversión simulada", percent(data.conversionRatePercent))
        MetricRow("Tiempo promedio", "${decimal(data.averageMinutes)} min", "Precio promedio", money(data.averagePrice))
        MetricRow("Total cotizado demo", money(data.totalPrices), "Costo directo promedio", money(data.averageDirectCost))
        MetricRow("Aporte bruto promedio*", money(data.averageContribution), "Margen directo no positivo", data.nonPositiveContributionCount.toString())
        Text("*Precio menos material estimado; no incluye mano de obra, gastos indirectos, impuestos ni otros costos.", style = MaterialTheme.typography.bodySmall)

        Text("Resultado de factibilidad", style = MaterialTheme.typography.titleLarge)
        data.feasibilityCounts.forEach { (status, count) ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(statusLabel(status), modifier = Modifier.weight(1f))
                    Text(count.toString(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Text("Inventario simulado", style = MaterialTheme.typography.titleLarge)
        data.inventory.forEach { item ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    Text("Saldo inicial: ${decimal(item.quantity)} ${item.unit}")
                    Text("Costo de material DEMO: ${money(item.unitCost)} / ${item.unit}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Text("Tarifas sintéticas", style = MaterialTheme.typography.titleLarge)
        data.priceRules.forEach { rule ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(rule.service, style = MaterialTheme.typography.titleMedium)
                    Text("${money(rule.rate)} / m² DEMO · ${rule.rule}")
                }
            }
        }

        Text("Escenarios de ejemplo", style = MaterialTheme.typography.titleLarge)
        data.sampleQuotes.forEach { quote -> SampleQuoteCard(quote) }
        Spacer(Modifier.height(8.dp))
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
            Text("${decimal(quote.length)} × ${decimal(quote.width)} m · ${quote.material}")
            Text("Requiere ${decimal(quote.required)} m² simulados; saldo usado: ${decimal(quote.available)} m²")
            Text("Precio demo: ${money(quote.simulatedPrice)} · Material demo: ${money(quote.directCost)}")
            if (quote.shortage > 0.0) {
                Text("Faltante estimado: ${decimal(quote.shortage)} m²", color = MaterialTheme.colorScheme.error)
            } else {
                Text("Material suficiente en este escenario", color = MaterialTheme.colorScheme.primary)
            }
            Text(statusLabel(quote.feasibility), style = MaterialTheme.typography.labelMedium)
            Text("Aporte directo estimado: ${money(quote.contribution)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun statusLabel(value: String): String = when (value) {
    "FACTIBLE_CON_REGLAS_SINTETICAS" -> "Factible según reglas sintéticas"
    "FALTA_STOCK" -> "Falta material en el escenario"
    "REVISAR_DATOS" -> "Revisar datos incompletos"
    "REVISAR_MARGEN_DIRECTO" -> "Revisar aporte directo"
    else -> value.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
}

private fun money(value: Double): String = String.format(Locale("es", "PE"), "S/ %.2f", value)
private fun decimal(value: Double): String = String.format(Locale("es", "PE"), "%.2f", value)
private fun percent(value: Double): String = String.format(Locale("es", "PE"), "%.1f%%", value)
