package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PantallaInicio(
    operadorSeleccionado: String,
    esModoOffline: Boolean,
    onCambiarOperador: (String) -> Unit,
    onAlternarModoRed: (Boolean) -> Unit,
    onIniciarNuevaCotizacion: () -> Unit,
    onIrAPanelAdmin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cotizador Leguía IA",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Operador actual: $operadorSeleccionado",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (esModoOffline) "Modo Offline" else "Modo Online")
            Switch(
                checked = esModoOffline,
                onCheckedChange = { onAlternarModoRed(it) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onIniciarNuevaCotizacion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Nueva Cotización")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onIrAPanelAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Panel de Administración")
        }
    }
}