package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.leguia.cotizadoria.R

@Composable
fun PantallaInicio(
    versionNombre: String,
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
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_legacy),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("LEGUÍA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.app_version_label, versionNombre),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

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
