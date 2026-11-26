package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.leguia.cotizadoria.components.CampoContrasena
import com.leguia.cotizadoria.firebase.CloudQuoteSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAccesoNube(
    signedIn: Boolean,
    busy: Boolean,
    statusMessage: String?,
    remoteQuotes: List<CloudQuoteSummary>?,
    onSignIn: (String, String) -> Unit,
    onSyncNow: () -> Unit,
    onLoadCloudQuotes: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Sincronización segura") }) }) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Las cotizaciones permanecen en este teléfono y se envía a Firebase solo un registro operativo sin texto del cliente ni nombre del operador.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (signedIn) {
                Text("Cuenta autenticada. Los registros nuevos se sincronizarán al guardarlos.")
                Button(onClick = onSyncNow, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    if (busy) CircularProgressIndicator() else Text("Sincronizar registros pendientes")
                }
                Button(onClick = onLoadCloudQuotes, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text("Consultar cotizaciones en Firebase")
                }
                remoteQuotes?.let { quotes ->
                    Text("Cotizaciones recuperadas de Firestore: ${quotes.size}")
                    if (quotes.isEmpty()) Text("No hay cotizaciones disponibles para esta organización.")
                    quotes.take(5).forEach { quote ->
                        val dimensions = listOfNotNull(quote.lengthM, quote.widthM)
                            .joinToString(" × ") { value -> value.toString() }
                        Text("${quote.id.take(8)} · ${quote.status} · ${dimensions.ifBlank { "sin medidas" }}")
                    }
                }
                Button(onClick = onSignOut, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                    Text("Cerrar sesión de nube")
                }
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.take(160) },
                    label = { Text("Correo de cuenta habilitada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                CampoContrasena(
                    value = password,
                    onValueChange = { password = it.copy(text = it.text.take(128)) },
                    label = "Contraseña",
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onSignIn(email, password.text) },
                    enabled = !busy && email.isNotBlank() && password.text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (busy) CircularProgressIndicator()
                    else Text("Iniciar sesión y sincronizar")
                }
                Text(
                    "Las cuentas las habilita el responsable en Firebase; no hay registro público desde la app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            statusMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}
