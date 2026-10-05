package com.leguia.cotizadoria.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.leguia.cotizadoria.data.ServicioEntity

@Composable
fun PantallaSeleccionServicio(
    listaServicios: List< ServicioEntity >,
    onServicioSeleccionado: (ServicioEntity) -> Unit,
    onCapturarPorVoz: () -> Unit,
    mensajeVoz: String?,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Seleccione un Servicio",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("También puedes dictar el servicio y las medidas. Revisa los campos antes de guardar.")
        Text("El reconocimiento usa el servicio de voz disponible en Android y puede requerir internet.")
        Button(onClick = onCapturarPorVoz, modifier = Modifier.fillMaxWidth()) {
            Text("Hablar requerimiento")
        }
        mensajeVoz?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(listaServicios) { servicio ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onServicioSeleccionado(servicio) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = servicio.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!servicio.descripcion.isNullOrEmpty()) {
                            Text(
                                text = servicio.descripcion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
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
