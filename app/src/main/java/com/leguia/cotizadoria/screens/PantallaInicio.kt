package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leguia.cotizadoria.data.CotizadorOperador

@Composable
fun PantallaInicio(
    operadorActual: CotizadorOperador,
    esOffline: Boolean,
    onCambiarOperador: (CotizadorOperador) -> Unit,
    onCambiarModoRed: (Boolean) -> Unit,
    onNavegarSeleccion: () -> Unit,
    onNavegarAdmin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "COTIZADOR LEGUÍA IA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Sistema de apoyo y trazabilidad",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Switch(checked = esOffline, onCheckedChange = onCambiarModoRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (esOffline) "Modo Offline (Manual)" else "Modo Online (Con IA)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (esOffline) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Identificación del Cotizador:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                CotizadorOperador.entries.forEach { operador ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = (operador == operadorActual),
                            onClick = { onCambiarOperador(operador) }
                        )
                        Text(
                            text = operador.nombreLegible,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNavegarSeleccion,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(text = "Nueva Cotización", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavegarAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Módulo Administración (PIN)")
        }
    }
}