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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    onAlternarModoRed: (Boolean) -> Unit,
    onIniciarNuevaCotizacion: () -> Unit,
    onIrAPanelAdmin: () -> Unit,
    cloudConfigured: Boolean = false,
    cloudSignedIn: Boolean = false,
    onIrASincronizacion: () -> Unit = {},
    cloudStatus: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_legacy),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("LEGUÍA", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.app_version_label, versionNombre),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        if (cloudConfigured) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Respaldo de registros", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (cloudSignedIn) "Cuenta conectada. Las nuevas cotizaciones se sincronizan con Firebase."
                        else "Solo en este teléfono. Inicia sesión para sincronizar nuevas cotizaciones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    cloudStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    OutlinedButton(onClick = onIrASincronizacion, modifier = Modifier.fillMaxWidth()) {
                        Text(if (cloudSignedIn) "Ver conexión de nube" else "Conectar cuenta de nube")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Atención", style = MaterialTheme.typography.titleMedium)
                Text("Operador: $operadorSeleccionado", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(if (esModoOffline) "Asistencia manual" else "Asistencia con IA", style = MaterialTheme.typography.titleSmall)
                        Text(if (esModoOffline) "La solicitud se completa a mano; se guarda en el teléfono." else "La interpretación necesita conexión con el backend.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = esModoOffline, onCheckedChange = onAlternarModoRed)
                }
            }
        }
        Button(onClick = onIniciarNuevaCotizacion, modifier = Modifier.fillMaxWidth()) {
            Text("＋  Nueva cotización")
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Gestión del negocio", style = MaterialTheme.typography.titleMedium)
                Text("Solicitudes, reportes, inventario y tarifas están disponibles para el responsable.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedButton(onClick = onIrAPanelAdmin, modifier = Modifier.fillMaxWidth()) {
                    Text("Abrir panel del responsable")
                }
            }
        }
        Text(
            if (cloudConfigured) "Room conserva la copia local; Firebase sincroniza registros minimizados."
            else "Los registros se almacenan en este dispositivo.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
