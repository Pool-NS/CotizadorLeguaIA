package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.leguia.cotizadoria.data.CotizacionEntity

@Composable
fun PantallaFormularioServicio(
    servicioId: Int,
    operador: String,
    onGuardarFormulario: (CotizacionEntity) -> Unit
) {
    var requerimiento by remember { mutableStateOf("") }
    var medidaLargo by remember { mutableStateOf("") }
    var medidaAncho by remember { mutableStateOf("") }
    var medidaAlto by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Formulario de Requerimiento")

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = requerimiento,
            onValueChange = { requerimiento = it },
            label = { Text("Requerimiento del Cliente") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = medidaLargo,
            onValueChange = { medidaLargo = it },
            label = { Text("Largo (cm/m)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = medidaAncho,
            onValueChange = { medidaAncho = it },
            label = { Text("Ancho (cm/m)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = medidaAlto,
            onValueChange = { medidaAlto = it },
            label = { Text("Alto (cm/m)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val cotizacion = CotizacionEntity(
                    id = 0,
                    codigoCotizacion = "COT-${System.currentTimeMillis()}",
                    servicioId = servicioId,
                    materialId = null,
                    operador = operador,
                    requerimientoCliente = requerimiento,
                    interpretacionIaJson = null,
                    corregidoPorHumano = false,
                    medidaLargo = medidaLargo.toDoubleOrNull(),
                    medidaAncho = medidaAncho.toDoubleOrNull(),
                    medidaAlto = medidaAlto.toDoubleOrNull(),
                    precioReferencial = 0.0,
                    descuentoMonto = 0.0,
                    descuentoPorcentaje = 0.0,
                    precioFinal = 0.0,
                    estado = "PENDIENTE",
                    fechaHoraInicio = System.currentTimeMillis(),
                    fechaHoraFin = null,
                    esModoOffline = true
                )
                onGuardarFormulario(cotizacion)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calcular Precio")
        }
    }
}