package com.leguia.cotizadoria.screens


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLoginAdmin(
    onAccesoConcedido: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var pinIngresado by remember { mutableStateOf("") }
    val pinCorrecto = "1234"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso Administrativo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Ingrese PIN de Seguridad", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Para consultar historial y exportar métricas", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = pinIngresado,
                onValueChange = { if (it.length <= 4) pinIngresado = it },
                label = { Text("PIN (4 dígitos)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (pinIngresado == pinCorrecto) {
                        onAccesoConcedido()
                    } else {
                        Toast.makeText(context, "PIN incorrecto. Intente de nuevo.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Ingresar")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al inicio")
            }
        }
    }
}