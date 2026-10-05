package com.leguia.cotizadoria.screens

import android.content.Context
import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

private const val ADMIN_PREFS = "admin_access"
private const val ADMIN_PIN_HASH = "pin_sha256"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLoginAdmin(onAccesoConcedido: () -> Unit, onVolver: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences(ADMIN_PREFS, Context.MODE_PRIVATE) }
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val isSetup = preferences.getString(ADMIN_PIN_HASH, null) == null

    Scaffold(topBar = { TopAppBar(title = { Text("Acceso del Jefe") }) }) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isSetup) "Configura el PIN privado del jefe antes de entregar esta instalación a los trabajadores."
                else "Ingresa el PIN privado del jefe para abrir administración.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(12); error = null },
                label = { Text(if (isSetup) "Nuevo PIN (mínimo 6 dígitos)" else "PIN del jefe") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (isSetup) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it.filter(Char::isDigit).take(12); error = null },
                    label = { Text("Confirmar PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (isSetup) {
                        when {
                            pin.length < 6 -> error = "El PIN debe tener al menos 6 dígitos."
                            pin != confirmation -> error = "Los PIN no coinciden."
                            else -> {
                                preferences.edit().putString(ADMIN_PIN_HASH, hashPin(pin)).apply()
                                onAccesoConcedido()
                            }
                        }
                    } else if (constantTimeEquals(preferences.getString(ADMIN_PIN_HASH, "").orEmpty(), hashPin(pin))) {
                        onAccesoConcedido()
                    } else error = "PIN incorrecto."
                }
            ) { Text(if (isSetup) "Crear acceso del jefe" else "Ingresar") }
            Spacer(Modifier.height(8.dp))
            Text(
                "El PIN se guarda en este teléfono. Configúralo el jefe antes de compartir cada instalación; sin cuentas o servidor, no se sincroniza entre teléfonos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
        }
    }
}

private fun hashPin(pin: String): String = Base64.encodeToString(
    MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8)),
    Base64.NO_WRAP
)

private fun constantTimeEquals(expected: String, actual: String): Boolean =
    MessageDigest.isEqual(expected.toByteArray(Charsets.UTF_8), actual.toByteArray(Charsets.UTF_8))
