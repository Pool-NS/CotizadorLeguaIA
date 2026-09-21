package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leguia.cotizadoria.data.CotizacionRegistro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaFormularioServicio(
    servicio: String,
    cotizacion: CotizacionRegistro?,
    esOffline: Boolean,
    onProcesarIA: (String) -> Unit,
    onContinuar: (String, String) -> Unit,
    onVolver: () -> Unit
) {
    var promptIa by remember { mutableStateOf("") }
    var ancho by remember { mutableStateOf("") }
    var largo by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cotizando: $servicio") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Asistente IA Gemini", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (esOffline) {
                        Text(text = "Sin conexión a Internet. Utilice el ingreso manual.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    } else {
                        OutlinedTextField(
                            value = promptIa,
                            onValueChange = { promptIa = it },
                            label = { Text("Describa la necesidad del cliente...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { onProcesarIA(promptIa) },
                            enabled = promptIa.isNotBlank()
                        ) {
                            Text("Interpretar con IA")
                        }
                    }
                    if (cotizacion?.datosInterpretadosIA != "N/A") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Resultado: ${cotizacion?.datosInterpretadosIA}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Text(text = "Confirmación Manual / Datos Técnicos", fontSize = 16.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(value = ancho, onValueChange = { ancho = it }, label = { Text("Ancho (m)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = largo, onValueChange = { largo = it }, label = { Text("Largo (m)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Material especificado") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val resumen = "Ancho: ${ancho}m, Largo: ${largo}m, Mat: $material"
                    val datosManuales = "Confirmado manualmente por cotizador"
                    onContinuar(resumen, datosManuales)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Generar Precio Referencial")
            }

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}