package com.leguia.cotizadoria.screens

import android.content.Context
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.leguia.cotizadoria.security.AdminPinHasher

private const val ADMIN_PREFS = "admin_access"
private const val ADMIN_PIN_HASH = "pin_pbkdf2_hash"
private const val ADMIN_PIN_SALT = "pin_salt"
private const val ADMIN_PIN_KDF = "pin_kdf"
private const val LEGACY_ADMIN_PIN_HASH = "pin_sha256"
private const val FAILED_ATTEMPTS = "failed_pin_attempts"
private const val LOCKED_UNTIL = "pin_locked_until"
private const val MIN_ADMIN_PIN_DIGITS = 8
private const val MAX_ADMIN_PIN_DIGITS = 12
private const val MAX_FAILED_ATTEMPTS = 5
private const val LOCK_DURATION_MILLIS = 60_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLoginAdmin(onAccesoConcedido: () -> Unit, onVolver: () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences(ADMIN_PREFS, Context.MODE_PRIVATE) }
    var pin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val isSetup = preferences.getString(ADMIN_PIN_HASH, null) == null &&
        preferences.getString(LEGACY_ADMIN_PIN_HASH, null) == null

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Acceso del jefe") },
            navigationIcon = {
                IconButton(onClick = onVolver, modifier = Modifier.semantics { contentDescription = "Regresar" }) {
                    Text("←", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isSetup) "Crea un PIN privado para el jefe."
                else "Ingresa el PIN privado del jefe.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter(Char::isDigit).take(MAX_ADMIN_PIN_DIGITS); error = null },
                label = { Text(if (isSetup) "Nuevo PIN (8–12 dígitos)" else "PIN del jefe") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (isSetup) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it.filter(Char::isDigit).take(MAX_ADMIN_PIN_DIGITS); error = null },
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
                            pin.length < MIN_ADMIN_PIN_DIGITS -> error = "El PIN debe tener al menos $MIN_ADMIN_PIN_DIGITS dígitos."
                            pin != confirmation -> error = "Los PIN no coinciden."
                            else -> {
                                val credential = AdminPinHasher.create(pin)
                                preferences.edit()
                                    .putString(ADMIN_PIN_SALT, credential.salt)
                                    .putString(ADMIN_PIN_HASH, credential.hash)
                                    .putString(ADMIN_PIN_KDF, credential.algorithm)
                                    .remove(FAILED_ATTEMPTS)
                                    .remove(LOCKED_UNTIL)
                                    .apply()
                                onAccesoConcedido()
                            }
                        }
                    } else {
                        val now = System.currentTimeMillis()
                        val lockedUntil = preferences.getLong(LOCKED_UNTIL, 0L)
                        if (lockedUntil > now) {
                            val remainingSeconds = ((lockedUntil - now + 999L) / 1000L).coerceAtLeast(1L)
                            error = "Demasiados intentos. Vuelve a probar en $remainingSeconds s."
                        } else {
                            val modernHash = preferences.getString(ADMIN_PIN_HASH, null)
                            val modernSalt = preferences.getString(ADMIN_PIN_SALT, null)
                            val modernAlgorithm = preferences.getString(ADMIN_PIN_KDF, null)
                            val legacyHash = preferences.getString(LEGACY_ADMIN_PIN_HASH, null)
                            val valid = if (modernHash != null && modernSalt != null && modernAlgorithm != null) {
                                AdminPinHasher.verify(pin, modernSalt, modernHash, modernAlgorithm)
                            } else {
                                legacyHash?.let { AdminPinHasher.verifyLegacySha256(pin, it) } == true
                            }
                            if (valid) {
                                val editor = preferences.edit()
                                    .remove(FAILED_ATTEMPTS)
                                    .remove(LOCKED_UNTIL)
                                if (modernHash == null || modernSalt == null || modernAlgorithm == null) {
                                    val upgraded = AdminPinHasher.create(pin)
                                    editor.putString(ADMIN_PIN_SALT, upgraded.salt)
                                        .putString(ADMIN_PIN_HASH, upgraded.hash)
                                        .putString(ADMIN_PIN_KDF, upgraded.algorithm)
                                        .remove(LEGACY_ADMIN_PIN_HASH)
                                }
                                editor.apply()
                                onAccesoConcedido()
                            } else {
                                val previousAttempts = if (lockedUntil > 0L) 0 else preferences.getInt(FAILED_ATTEMPTS, 0)
                                val attempts = previousAttempts + 1
                                if (attempts >= MAX_FAILED_ATTEMPTS) {
                                    preferences.edit()
                                        .putInt(FAILED_ATTEMPTS, 0)
                                        .putLong(LOCKED_UNTIL, now + LOCK_DURATION_MILLIS)
                                        .apply()
                                    error = "Demasiados intentos. Vuelve a probar en 60 s."
                                } else {
                                    preferences.edit().putInt(FAILED_ATTEMPTS, attempts).remove(LOCKED_UNTIL).apply()
                                    error = "PIN incorrecto. Intento $attempts de $MAX_FAILED_ATTEMPTS."
                                }
                            }
                        }
                    }
                }
            ) { Text(if (isSetup) "Crear acceso del jefe" else "Ingresar") }
            Spacer(Modifier.height(8.dp))
            Text(
                "PIN local de este teléfono; no es una cuenta compartida ni acceso a servidor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
