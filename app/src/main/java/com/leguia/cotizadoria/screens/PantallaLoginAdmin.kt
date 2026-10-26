package com.leguia.cotizadoria.screens

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
private const val MAX_ADMIN_PASSWORD_LENGTH = 64
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
    var mustUpgradePassword by remember { mutableStateOf(false) }
    val isSetup = preferences.getString(ADMIN_PIN_HASH, null) == null &&
        preferences.getString(LEGACY_ADMIN_PIN_HASH, null) == null
    val isCredentialSetup = isSetup || mustUpgradePassword

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
                if (isCredentialSetup && mustUpgradePassword) "Actualiza tu contraseña para cumplir la política del proyecto."
                else if (isCredentialSetup) "Crea una contraseña privada para el jefe."
                else "Ingresa la contraseña privada del jefe.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.take(MAX_ADMIN_PASSWORD_LENGTH); error = null },
                label = { Text(if (isCredentialSetup) "Contraseña (12–64 caracteres)" else "Contraseña del jefe") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (isCredentialSetup) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it.take(MAX_ADMIN_PASSWORD_LENGTH); error = null },
                    label = { Text("Confirmar contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val result = processAdminLogin(pin, confirmation, isCredentialSetup, preferences)
                    error = result.message
                    mustUpgradePassword = result.requiresPasswordUpgrade
                    if (result.clearInputs) {
                        pin = ""
                        confirmation = ""
                    }
                    if (result.granted) onAccesoConcedido()
                }
            ) {
                Text(
                    when {
                        mustUpgradePassword -> "Actualizar contraseña"
                        isSetup -> "Crear acceso del jefe"
                        else -> "Ingresar"
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Contraseña guardada solo en este teléfono; no es una cuenta compartida ni acceso a servidor.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private data class AdminLoginResult(
    val granted: Boolean = false,
    val message: String? = null,
    val requiresPasswordUpgrade: Boolean = false,
    val clearInputs: Boolean = false
)

private fun processAdminLogin(
    password: String,
    confirmation: String,
    isCredentialSetup: Boolean,
    preferences: SharedPreferences
): AdminLoginResult = if (isCredentialSetup) {
    createAdminCredential(password, confirmation, preferences)
} else {
    authenticateAdmin(password, preferences, System.currentTimeMillis())
}

private fun createAdminCredential(
    password: String,
    confirmation: String,
    preferences: SharedPreferences
): AdminLoginResult {
    val passwordError = AdminPinHasher.validateNewPassword(password)
    if (passwordError != null) return AdminLoginResult(message = passwordError)
    if (password != confirmation) return AdminLoginResult(message = "Las contraseñas no coinciden.")

    val credential = AdminPinHasher.create(password)
    preferences.edit()
        .putString(ADMIN_PIN_SALT, credential.salt)
        .putString(ADMIN_PIN_HASH, credential.hash)
        .putString(ADMIN_PIN_KDF, credential.algorithm)
        .remove(FAILED_ATTEMPTS)
        .remove(LOCKED_UNTIL)
        .apply()
    return AdminLoginResult(granted = true)
}

private fun authenticateAdmin(
    password: String,
    preferences: SharedPreferences,
    now: Long
): AdminLoginResult {
    val lockedUntil = preferences.getLong(LOCKED_UNTIL, 0L)
    if (lockedUntil > now) {
        val remainingSeconds = ((lockedUntil - now + 999L) / 1000L).coerceAtLeast(1L)
        return AdminLoginResult(message = "Demasiados intentos. Vuelve a probar en $remainingSeconds s.")
    }

    val valid = verifyStoredCredential(password, preferences)
    if (!valid) return registerFailedAttempt(preferences, lockedUntil, now)

    migrateLegacyCredentialIfNeeded(password, preferences)
    if (AdminPinHasher.validateNewPassword(password) != null) {
        return AdminLoginResult(
            message = "Tu contraseña anterior es válida. Crea una nueva con la política actual.",
            requiresPasswordUpgrade = true,
            clearInputs = true
        )
    }
    return AdminLoginResult(granted = true)
}

private fun verifyStoredCredential(password: String, preferences: SharedPreferences): Boolean {
    val hash = preferences.getString(ADMIN_PIN_HASH, null)
    val salt = preferences.getString(ADMIN_PIN_SALT, null)
    val algorithm = preferences.getString(ADMIN_PIN_KDF, null)
    if (hash != null && salt != null && algorithm != null) {
        return AdminPinHasher.verify(password, salt, hash, algorithm)
    }
    return preferences.getString(LEGACY_ADMIN_PIN_HASH, null)
        ?.let { AdminPinHasher.verifyLegacySha256(password, it) } == true
}

private fun migrateLegacyCredentialIfNeeded(password: String, preferences: SharedPreferences) {
    val hasModernCredential = preferences.getString(ADMIN_PIN_HASH, null) != null &&
        preferences.getString(ADMIN_PIN_SALT, null) != null &&
        preferences.getString(ADMIN_PIN_KDF, null) != null
    val editor = preferences.edit().remove(FAILED_ATTEMPTS).remove(LOCKED_UNTIL)
    if (!hasModernCredential) {
        val credential = AdminPinHasher.create(password)
        editor.putString(ADMIN_PIN_SALT, credential.salt)
            .putString(ADMIN_PIN_HASH, credential.hash)
            .putString(ADMIN_PIN_KDF, credential.algorithm)
            .remove(LEGACY_ADMIN_PIN_HASH)
    }
    editor.apply()
}

private fun registerFailedAttempt(
    preferences: SharedPreferences,
    previousLockTime: Long,
    now: Long
): AdminLoginResult {
    val previousAttempts = if (previousLockTime > 0L) 0 else preferences.getInt(FAILED_ATTEMPTS, 0)
    val attempts = previousAttempts + 1
    if (attempts >= MAX_FAILED_ATTEMPTS) {
        preferences.edit()
            .putInt(FAILED_ATTEMPTS, 0)
            .putLong(LOCKED_UNTIL, now + LOCK_DURATION_MILLIS)
            .apply()
        return AdminLoginResult(message = "Demasiados intentos. Vuelve a probar en 60 s.")
    }
    preferences.edit().putInt(FAILED_ATTEMPTS, attempts).remove(LOCKED_UNTIL).apply()
    return AdminLoginResult(message = "Contraseña incorrecta. Intento $attempts de $MAX_FAILED_ATTEMPTS.")
}
