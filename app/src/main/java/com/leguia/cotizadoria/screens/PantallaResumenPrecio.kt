package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.leguia.cotizadoria.data.CotizacionEntity

@Composable
fun PantallaResumenPrecio(
    cotizacion: CotizacionEntity?,
    onFinalizarCotizacion: (CotizacionEntity) -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resumen de Precio",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cotizacion != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Código: " + cotizacion.codigoCotizacion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Requerimiento: " + cotizacion.requerimientoCliente)
                    Text(text = "Operador: " + cotizacion.operador)

                    if (cotizacion.medidaLargo != null) {
                        Text(text = "Medidas: " + cotizacion.medidaLargo + " x " + cotizacion.medidaAncho + " x " + cotizacion.medidaAlto)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Precio Final: Soles " + cotizacion.precioFinal,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onFinalizarCotizacion(cotizacion) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar y Guardar Cotización")
            }
        } else {
            Text(text = "No hay datos de cotización disponibles.")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}