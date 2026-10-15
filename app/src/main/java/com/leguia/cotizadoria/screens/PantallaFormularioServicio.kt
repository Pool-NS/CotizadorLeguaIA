package com.leguia.cotizadoria.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

import com.leguia.cotizadoria.data.CotizacionEntity
import com.leguia.cotizadoria.ai.RequirementInterpretation
import com.leguia.cotizadoria.domain.SpokenRequirementParser
import org.json.JSONObject
import com.leguia.cotizadoria.components.EncabezadoPantalla

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
    var medidaAlto by remember(servicioId) { mutableStateOf("") }
    var errorFormulario by remember { mutableStateOf<String?>(null) }
    var iaAplicada by remember { mutableStateOf(false) }
    var corregidaPorPersona by remember { mutableStateOf(false) }
    val isCarpas = servicioNombre.equals("Carpas", ignoreCase = true)
    val isMoto = servicioNombre.equals("Tapizado de moto", ignoreCase = true)
    val opcionesMaterial = when {
        isCarpas -> listOf("Lino económico", "Lino liviano", "Lino pesado")
        servicioNombre.equals("Toldos", ignoreCase = true) -> listOf("Tarflex 200 gramos", "Tarflex 300 gramos", "Lona orillo azul", "Lona orillo rojo")
        else -> emptyList()
    }

    LaunchedEffect(transcripcionInicial) {
        transcripcionInicial?.takeIf(String::isNotBlank)?.let(onInterpretarRequerimiento)
    }
    LaunchedEffect(interpretacionIA) {
        interpretacionIA?.let { interpretation ->
            interpretation.dimensions["largo"]?.let { medidaLargo = it.toString() }
            interpretation.dimensions["ancho"]?.let { medidaAncho = it.toString() }
            interpretation.dimensions["alto"]?.let { medidaAlto = it.toString() }
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
        EncabezadoPantalla("Nueva cotización", onVolver, subtitulo = servicioNombre)
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

        Text("1. Elige las medidas", style = MaterialTheme.typography.titleMedium)
        Text("Largo × ancho en metros. Desliza para ver más tamaños.", style = MaterialTheme.typography.bodySmall)
        val medidasFrecuentes = listOf(
            "2 × 2" to (2.0 to 2.0), "2.5 × 2" to (2.5 to 2.0), "3 × 2" to (3.0 to 2.0),
            "3 × 4" to (3.0 to 4.0), "4 × 3" to (4.0 to 3.0), "4 × 4" to (4.0 to 4.0),
            "5 × 4" to (5.0 to 4.0), "6 × 3" to (6.0 to 3.0), "6 × 4" to (6.0 to 4.0),
            "6 × 5" to (6.0 to 5.0), "2 × 6" to (2.0 to 6.0), "6 × 6" to (6.0 to 6.0),
            "7 × 4" to (7.0 to 4.0)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(medidasFrecuentes) { (label, dimensions) ->
                val selected = medidaLargo.toDoubleOrNull() == dimensions.first && medidaAncho.toDoubleOrNull() == dimensions.second
                FilterChip(selected = selected, onClick = {
                    medidaLargo = dimensions.first.toString()
                    medidaAncho = dimensions.second.toString()
                    if (iaAplicada) corregidaPorPersona = true
                }, label = { Text("$label m") })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = medidaLargo,
                onValueChange = { medidaLargo = it; if (iaAplicada) corregidaPorPersona = true },
                label = { Text("Largo (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f), singleLine = true
            )
            OutlinedTextField(
                value = medidaAncho,
                onValueChange = { medidaAncho = it; if (iaAplicada) corregidaPorPersona = true },
                label = { Text("Ancho (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f), singleLine = true
            )
        }
        if (isCarpas) {
            val length = medidaLargo.replace(',', '.').toDoubleOrNull()
            val width = medidaAncho.replace(',', '.').toDoubleOrNull()
            val largestDimension = listOfNotNull(length, width).maxOrNull()
            if (length != null && width != null && largestDimension != null && length > 0.0 && width > 0.0) {
                Text(
                    "Estructura según la regla indicada: ${if (largestDimension >= 4.0) "tubo galvanizado" else "tubo electro"}.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        OutlinedTextField(
            value = medidaAlto,
            onValueChange = { medidaAlto = it; if (iaAplicada) corregidaPorPersona = true },
            label = { Text("Altura (m, opcional)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        Text("2. Elige el material", style = MaterialTheme.typography.titleMedium)
        when {
            opcionesMaterial.isNotEmpty() -> {
                Text("Selecciona una opción o indica otra. El precio requiere una tarifa aprobada para el material exacto.", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(opcionesMaterial) { option ->
                        FilterChip(selected = material == option, onClick = {
                            material = option
                            if (iaAplicada) corregidaPorPersona = true
                        }, label = { Text(option) })
                    }
                    item {
                        FilterChip(selected = material.isBlank(), onClick = {
                            material = ""
                            if (iaAplicada) corregidaPorPersona = true
                        }, label = { Text("Otro") })
                    }
                }
                if (material.isBlank()) {
                    OutlinedTextField(
                        value = material,
                        onValueChange = { material = it; if (iaAplicada) corregidaPorPersona = true },
                        label = { Text("Escribe el material") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                }
                Text("Las carpas se cotizan con lino económico, liviano o pesado. Los toldos usan Tarflex o lona según la opción elegida.", style = MaterialTheme.typography.bodySmall)
            }
            isMoto -> {
                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it; if (iaAplicada) corregidaPorPersona = true },
                    label = { Text("Material o acabado (según lo acordado)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Text("El tipo o modelo de moto se especifica abajo. Confirma material y tarifa con el jefe.", style = MaterialTheme.typography.bodySmall)
            }
            else -> {
            OutlinedTextField(
                value = material,
                onValueChange = { material = it; if (iaAplicada) corregidaPorPersona = true },
                label = { Text("Material mencionado (confirmar)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            }
        }

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
                val alto = medidaAlto.replace(',', '.').toDoubleOrNull()
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
                if (medidaAlto.isNotBlank() && (alto == null || !alto.isFinite() || alto <= 0.0)) {
                    errorFormulario = "La altura opcional debe ser un número mayor que cero."
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
                    medidaAlto = alto,
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
    }
}
