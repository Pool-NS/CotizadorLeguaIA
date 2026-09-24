package com.leguia.cotizadoria.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Importamos las entidades de Room
import com.leguia.cotizadoria.data.CotizacionEntity

@Composable
fun PantallaPanelAdmin(
    listaCotizaciones: List< CotizacionEntity >,
    onVolver: () -> Unit
) {
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
                text = "Panel de Administración",
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedButton(onClick = onVolver) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Historial de Cotizaciones (${listaCotizaciones.size})",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(listaCotizaciones) { cotizacion ->
                TarjetaCotizacionItem(cotizacion = cotizacion)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TarjetaCotizacionItem(cotizacion: CotizacionEntity) {
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
            Text(
                text = "Precio Final: S/ ${cotizacion.precioFinal}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}