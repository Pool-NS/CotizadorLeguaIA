package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.ai.RequirementInterpretation
import com.leguia.cotizadoria.domain.SpokenRequirementParser
import org.json.JSONObject

@Composable
fun PantallaFormularioServicio(
    servicioId: Int,
    servicioNombre: String,
    operador: String,
    esModoOffline: Boolean,
    transcripcionInicial: String?,
    interpretacionIA: RequirementInterpretation?,
    interpretacionIALoading: Boolean,
    errorIA: String?,
    onInterpretarRequerimiento: (String) -> Unit,
    onGuardarFormulario: (CotizacionEntity) -> Unit,
    onVolver: () -> Unit
) {
    val datosVoz = remember(transcripcionInicial) {
        transcripcionInicial?.let(SpokenRequirementParser::interpret)
    }
    var requerimiento by remember(transcripcionInicial) { mutableStateOf(transcripcionInicial.orEmpty()) }
    var medidaLargo by remember(transcripcionInicial) { mutableStateOf(datosVoz?.largo?.toString().orEmpty()) }
    var medidaAncho by remember(transcripcionInicial) { mutableStateOf(datosVoz?.ancho?.toString().orEmpty()) }
    var tipoMoto by remember(servicioId) { mutableStateOf("") }
    var material by remember(servicioId) { mutableStateOf("") }
    var carpaCerrada by remember(servicioId) { mutableStateOf(false) }
    var cantidadVentanas by remember(servicioId) { mutableStateOf("") }
    var cantidadPuertas by remember(servicioId) { mutableStateOf("") }
    var errorFormulario by remember { mutableStateOf<String?>(null) }
    var iaAplicada by remember { mutableStateOf(false) }
    var corregidaPorPersona by remember { mutableStateOf(false) }
    val isCarpas = servicioNombre.equals("Carpas", ignoreCase = true)
    val isMoto = servicioNombre.equals("Tapizado de moto", ignoreCase = true)

    LaunchedEffect(transcripcionInicial) {
        transcripcionInicial?.takeIf(String::isNotBlank)?.let(onInterpretarRequerimiento)
    }
    LaunchedEffect(interpretacionIA) {
        interpretacionIA?.let { interpretation ->
            interpretation.dimensions["largo"]?.let { medidaLargo = it.toString() }
            interpretation.dimensions["ancho"]?.let { medidaAncho = it.toString() }
            interpretation.characteristics["material"]?.let { material = it }
            interpretation.characteristics["tipoMoto"]?.let { tipoMoto = it }
            interpretation.characteristics["tipoCarpa"]?.let { tent ->
                carpaCerrada = tent.equals("CERRADA", ignoreCase = true)
            }
            interpretation.characteristics["ventanas"]?.toIntOrNull()?.let { cantidadVentanas = it.toString() }
            interpretation.characteristics["puertas"]?.toIntOrNull()?.let { cantidadPuertas = it.toString() }
            iaAplicada = true
            corregidaPorPersona = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Nueva cotización", style = MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(text = servicioNombre, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text("Completa largo y ancho para cotizar.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (transcripcionInicial != null) {
            Text("Servicio detectado: $servicioNombre. Revisa la transcripción y las medidas antes de guardar.")
            Text("Unidades por confirmar con el cliente.")
            if (datosVoz?.dimensionsNeedingReview?.isNotEmpty() == true) {
                Text("Hay medidas repetidas o ambiguas: ${datosVoz.dimensionsNeedingReview.joinToString()}.", color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = requerimiento,
            onValueChange = { requerimiento = it; if (iaAplicada) corregidaPorPersona = true },
            label = { Text("Requerimiento del Cliente") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onInterpretarRequerimiento(requerimiento) },
                enabled = requerimiento.isNotBlank() && !interpretacionIALoading
            ) { Text(if (interpretacionIALoading) "Interpretando…" else "Interpretar con IA") }
            if (interpretacionIALoading) Text("Esperando propuesta")
        }
        errorIA?.let { Text("IA no disponible: $it. Puedes completar los datos manualmente.", color = Color.Red) }
        interpretacionIA?.let { interpretation ->
            Text("Propuesta de IA: ${interpretation.serviceSuggestion ?: "servicio no identificado"}. Revisa y corrige antes de continuar.")
            if (interpretation.missingFields.isNotEmpty()) {
                Text("Datos por confirmar: ${interpretation.missingFields.joinToString()}")
            }
            if (interpretation.ambiguities.isNotEmpty()) {
                Text("Ambigüedades: ${interpretation.ambiguities.joinToString()}", color = Color.Red)
            }
            if (interpretation.serviceSuggestion != null && !interpretation.serviceSuggestion.equals(servicioNombre, ignoreCase = true)) {
                Text("La IA sugirió otro servicio. Vuelve a servicios para cambiarlo y revisa el requerimiento.", color = Color.Red)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = medidaLargo,
            onValueChange = { medidaLargo = it; if (iaAplicada) corregidaPorPersona = true },
            label = { Text("Largo (unidad por validar)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = medidaAncho,
            onValueChange = { medidaAncho = it; if (iaAplicada) corregidaPorPersona = true },
            label = { Text("Ancho (unidad por validar)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = material,
            onValueChange = { material = it; if (iaAplicada) corregidaPorPersona = true },
            label = { Text("Material mencionado (revisar)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (isCarpas) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Características de la carpa", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = carpaCerrada, onCheckedChange = { carpaCerrada = it; if (iaAplicada) corregidaPorPersona = true })
                        Text("Carpa cerrada")
                    }
                    if (carpaCerrada) {
                        OutlinedTextField(
                            value = cantidadVentanas,
                            onValueChange = { cantidadVentanas = it.filter(Char::isDigit).take(2); if (iaAplicada) corregidaPorPersona = true },
                            label = { Text("Cantidad de ventanas") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                        OutlinedTextField(
                            value = cantidadPuertas,
                            onValueChange = { cantidadPuertas = it.filter(Char::isDigit).take(2); if (iaAplicada) corregidaPorPersona = true },
                            label = { Text("Cantidad de puertas (inicia en 1)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                        )
                    }
                }
            }
        }

        if (isMoto) {
            OutlinedTextField(
                value = tipoMoto,
                onValueChange = { tipoMoto = it; if (iaAplicada) corregidaPorPersona = true },
                label = { Text("Tipo o modelo de moto") },
                placeholder = { Text("Ejemplo: lineal, scooter, marca y modelo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        errorFormulario?.let { Text(text = it, color = Color.Red) }

        Button(
            onClick = {
                val largo = medidaLargo.replace(',', '.').toDoubleOrNull()
                val ancho = medidaAncho.replace(',', '.').toDoubleOrNull()
                if (requerimiento.isBlank()) {
                    errorFormulario = "Describa el requerimiento del cliente."
                    return@Button
                }
                if (interpretacionIA?.serviceSuggestion != null &&
                    !interpretacionIA.serviceSuggestion.equals(servicioNombre, ignoreCase = true)
                ) {
                    errorFormulario = "La IA sugirió ${interpretacionIA.serviceSuggestion}. Vuelve a servicios, selecciona el correcto y revisa los datos."
                    return@Button
                }
                if (medidaLargo.isBlank() || medidaAncho.isBlank()) {
                    errorFormulario = "Ingresa largo y ancho para guardar la cotización."
                    return@Button
                }
                val dimensiones = listOf(medidaLargo to largo, medidaAncho to ancho)
                if (dimensiones.any { (raw, parsed) -> raw.isNotBlank() && (parsed == null || !parsed.isFinite() || parsed <= 0.0) }) {
                    errorFormulario = "Cada medida ingresada debe ser positiva y usar un decimal válido."
                    return@Button
                }
                val ventanas = if (isCarpas && carpaCerrada) cantidadVentanas.toIntOrNull() else null
                val puertas = if (isCarpas && carpaCerrada) cantidadPuertas.toIntOrNull() else null
                if (isCarpas && carpaCerrada && (ventanas == null || puertas == null || puertas < 1)) {
                    errorFormulario = "Indica cuántas ventanas necesita y registra al menos una puerta para la carpa cerrada."
                    return@Button
                }
                errorFormulario = null
                val cotizacion = CotizacionEntity(
                    id = 0,
                    codigoCotizacion = "COT-${System.currentTimeMillis()}",
                    servicioId = servicioId,
                    materialId = null,
                    operador = operador,
                    requerimientoCliente = requerimiento.trim(),
                    interpretacionIaJson = interpretacionIA?.let { interpretation ->
                        JSONObject()
                            .put("servicio_sugerido", interpretation.serviceSuggestion ?: JSONObject.NULL)
                            .put("largo_sugerido", interpretation.dimensions["largo"] ?: JSONObject.NULL)
                            .put("ancho_sugerido", interpretation.dimensions["ancho"] ?: JSONObject.NULL)
                            .put("caracteristicas_sugeridas", JSONObject().apply {
                                interpretation.characteristics.forEach { (key, value) -> put(key, value) }
                            })
                            .put("campos_faltantes", interpretation.missingFields)
                            .put("ambiguedades", interpretation.ambiguities)
                            .put("requiere_revision", interpretation.reviewRequired)
                            .put("revisada_por_persona", true)
                            .toString()
                    },
                    corregidoPorHumano = corregidaPorPersona,
                    medidaLargo = largo,
                    medidaAncho = ancho,
                    medidaAlto = null,
                    precioReferencial = 0.0,
                    descuentoMonto = 0.0,
                    descuentoPorcentaje = 0.0,
                    precioFinal = 0.0,
                    estado = "PENDIENTE",
                    fechaHoraInicio = System.currentTimeMillis(),
                    fechaHoraFin = null,
                    esModoOffline = esModoOffline,
                    tipoMoto = tipoMoto.trim().takeIf { isMoto && it.isNotBlank() },
                    tipoCarpa = if (isCarpas) if (carpaCerrada) "CERRADA" else "ABIERTA" else null,
                    cantidadVentanas = ventanas,
                    cantidadPuertas = puertas,
                    materialDescripcion = material.trim().takeIf(String::isNotBlank)
                )
                onGuardarFormulario(cotizacion)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Revisar cotización")
        }
        OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth()) { Text("Volver a servicios") }
    }
}
