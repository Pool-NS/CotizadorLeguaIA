package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Importamos las entidades de Room
import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.data.StockMaterialRow
import com.leguia.cotizadoria.data.ServicioEntity
import com.leguia.cotizadoria.data.ParametroPrecioEntity
import com.leguia.cotizadoria.data.SyntheticDemoDashboard
import com.leguia.cotizadoria.domain.QuoteStatus
import java.util.Locale

@Composable
fun PantallaPanelAdmin(
    listaCotizaciones: List< CotizacionEntity >,
    listaStock: List<StockMaterialRow>,
    servicios: List<ServicioEntity>,
    tarifas: List<ParametroPrecioEntity>,
    onGuardarTarifa: (Int, Double, Double, String, String, Double) -> Unit,
    onRegistrarIngreso: (String, String, Double, String, (String?) -> Unit) -> Unit,
    onRegistrarResultado: (CotizacionEntity, QuoteStatus) -> Unit,
    demoData: SyntheticDemoDashboard,
    onVolver: () -> Unit
) {
    var mostrarInventario by remember { mutableStateOf(false) }
    var mostrarTarifas by remember { mutableStateOf(false) }
    var mostrarDemo by remember { mutableStateOf(false) }
    if (mostrarTarifas) {
        PantallaTarifas(servicios, tarifas, onGuardarTarifa, onVolver = { mostrarTarifas = false })
        return
    }
    if (mostrarInventario) {
        PantallaInventario(stock = listaStock, onRegistrarIngreso = onRegistrarIngreso, onVolver = { mostrarInventario = false })
        return
    }
    if (mostrarDemo) {
        PantallaDemoDashboard(data = demoData, onVolver = { mostrarDemo = false })
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(
                text = "Panel del Jefe",
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedButton(onClick = onVolver) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            Button(onClick = { mostrarInventario = true }, modifier = Modifier.weight(1f)) {
                Text("Materiales y stock")
            }
            OutlinedButton(onClick = { mostrarTarifas = true }, modifier = Modifier.weight(1f)) {
                Text("Tarifas")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { mostrarDemo = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Dashboard demo · 200 datos sintéticos")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Indicadores de cotizaciones guardadas en este teléfono",
            style = MaterialTheme.typography.titleLarge
        )
        val resolvedQuotes = listaCotizaciones.count {
            it.estado == QuoteStatus.CONCRETADA.name || it.estado == QuoteStatus.NO_CONCRETADA.name
        }
        val conversionPercent = if (resolvedQuotes == 0) 0.0 else
            listaCotizaciones.count { it.estado == QuoteStatus.CONCRETADA.name } * 100.0 / resolvedQuotes
        val quotedValue = listaCotizaciones.filter { it.estado != "PENDIENTE_PRECIO" }.sumOf { it.precioFinal }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            MetricCard("Solicitudes", listaCotizaciones.size.toString(), Modifier.weight(1f))
            MetricCard("Pendientes", listaCotizaciones.count { it.estado == QuoteStatus.PENDIENTE.name || it.estado == "PENDIENTE_PRECIO" }.toString(), Modifier.weight(1f))
            MetricCard("Stock", listaStock.size.toString(), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            MetricCard("Concretadas", listaCotizaciones.count { it.estado == QuoteStatus.CONCRETADA.name }.toString(), Modifier.weight(1f))
            MetricCard("No concretadas", listaCotizaciones.count { it.estado == QuoteStatus.NO_CONCRETADA.name }.toString(), Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            MetricCard("Valor cotizado", String.format(Locale("es", "PE"), "S/ %.2f", quotedValue), Modifier.weight(1f))
            MetricCard("Conversión cerrada", String.format(Locale("es", "PE"), "%.1f%%", conversionPercent), Modifier.weight(1f))
        }
        Text("La conversión usa cotizaciones concretadas / (concretadas + no concretadas). Los datos de este panel son locales a este teléfono.", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Historial de cotizaciones", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(listaCotizaciones) { cotizacion ->
                TarjetaCotizacionItem(cotizacion = cotizacion, onRegistrarResultado = onRegistrarResultado)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun TarjetaCotizacionItem(
    cotizacion: CotizacionEntity,
    onRegistrarResultado: (CotizacionEntity, QuoteStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Código: ${cotizacion.codigoCotizacion}",
                style = MaterialTheme.typography.titleSmall
            )
            Text(text = "Operador: ${cotizacion.operador}")
            Text(text = "Estado: ${cotizacion.estado}")
            if (cotizacion.estado == "PENDIENTE_PRECIO") {
                Text("Precio pendiente: falta configurar una regla aprobada")
            } else {
                Text("Precio Final: S/ ${cotizacion.precioFinal}", style = MaterialTheme.typography.bodyMedium)
            }
            if (cotizacion.estado == QuoteStatus.PENDIENTE.name && cotizacion.precioReferencial > 0.0) {
                Row {
                    Button(onClick = { onRegistrarResultado(cotizacion, QuoteStatus.CONCRETADA) }) {
                        Text("Concretada")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onRegistrarResultado(cotizacion, QuoteStatus.NO_CONCRETADA) }) {
                        Text("No concretada")
                    }
                }
            }
        }
    }
}
