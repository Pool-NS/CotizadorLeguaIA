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
    val opcionesMaterial = materialOptions(servicioNombre, isCarpas)

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

        SpeechRequirementNotice(
            serviceName = servicioNombre,
            dimensionsToReview = datosVoz?.dimensionsNeedingReview.orEmpty(),
            visible = transcripcionInicial != null
        )

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
        AiInterpretationSummary(interpretacionIA, servicioNombre)

        Spacer(modifier = Modifier.height(8.dp))

        Text("1. Elige las medidas", style = MaterialTheme.typography.titleMedium)
        Text("Largo × ancho en metros. Desliza para ver más tamaños.", style = MaterialTheme.typography.bodySmall)
        DimensionFields(
            length = medidaLargo,
            width = medidaAncho,
            height = medidaAlto,
            showStructureGuidance = isCarpas,
            onLengthChange = { medidaLargo = it; if (iaAplicada) corregidaPorPersona = true },
            onWidthChange = { medidaAncho = it; if (iaAplicada) corregidaPorPersona = true },
            onHeightChange = { medidaAlto = it; if (iaAplicada) corregidaPorPersona = true },
            onSelectPreset = { length, width ->
                medidaLargo = length.toString()
                medidaAncho = width.toString()
                if (iaAplicada) corregidaPorPersona = true
            }
        )

        Text("2. Elige el material", style = MaterialTheme.typography.titleMedium)
        MaterialFields(
            serviceOptions = opcionesMaterial,
            isMotorcycle = isMoto,
            value = material,
            onValueChange = { material = it; if (iaAplicada) corregidaPorPersona = true }
        )

        TentFields(
            visible = isCarpas,
            closed = carpaCerrada,
            windows = cantidadVentanas,
            doors = cantidadPuertas,
            onClosedChange = { carpaCerrada = it; if (iaAplicada) corregidaPorPersona = true },
            onWindowsChange = { cantidadVentanas = it.filter(Char::isDigit).take(2); if (iaAplicada) corregidaPorPersona = true },
            onDoorsChange = { cantidadPuertas = it.filter(Char::isDigit).take(2); if (iaAplicada) corregidaPorPersona = true }
        )

        MotorcycleField(isMotorcycle = isMoto, value = tipoMoto) {
            tipoMoto = it
            if (iaAplicada) corregidaPorPersona = true
        }

        Spacer(modifier = Modifier.height(8.dp))

        errorFormulario?.let { Text(text = it, color = Color.Red) }

        Button(
            onClick = {
                val input = QuoteFormInput(
                    serviceId = servicioId,
                    serviceName = servicioNombre,
                    operator = operador,
                    offline = esModoOffline,
                    requirement = requerimiento,
                    lengthText = medidaLargo,
                    widthText = medidaAncho,
                    heightText = medidaAlto,
                    material = material,
                    motorcycleType = tipoMoto,
                    isCarpas = isCarpas,
                    isMoto = isMoto,
                    tentClosed = carpaCerrada,
                    windowsText = cantidadVentanas,
                    doorsText = cantidadPuertas,
                    correctedByPerson = corregidaPorPersona,
                    interpretation = interpretacionIA
                )
                val error = validateQuoteForm(input)
                if (error != null) {
                    errorFormulario = error
                } else {
                    errorFormulario = null
                    onGuardarFormulario(input.toCotizacion())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Revisar cotización")
        }
    }
}

private fun materialOptions(serviceName: String, isCarpas: Boolean): List<String> = when {
    isCarpas -> listOf("Lino económico", "Lino liviano", "Lino pesado")
    serviceName.equals("Toldos", ignoreCase = true) ->
        listOf("Tarflex 200 gramos", "Tarflex 300 gramos", "Lona orillo azul", "Lona orillo rojo")
    else -> emptyList()
}

@Composable
private fun SpeechRequirementNotice(serviceName: String, dimensionsToReview: List<String>, visible: Boolean) {
    if (!visible) return
    Text("Servicio detectado: $serviceName. Revisa la transcripción y las medidas antes de guardar.")
    Text("Unidades por confirmar con el cliente.")
    if (dimensionsToReview.isNotEmpty()) {
        Text("Hay medidas repetidas o ambiguas: ${dimensionsToReview.joinToString()}.", color = Color.Red)
    }
}

@Composable
private fun AiInterpretationSummary(interpretation: RequirementInterpretation?, serviceName: String) {
    if (interpretation == null) return
    Text("Propuesta de IA: ${interpretation.serviceSuggestion ?: "servicio no identificado"}. Revisa y corrige antes de continuar.")
    if (interpretation.missingFields.isNotEmpty()) {
        Text("Datos por confirmar: ${interpretation.missingFields.joinToString()}")
    }
    if (interpretation.ambiguities.isNotEmpty()) {
        Text("Ambigüedades: ${interpretation.ambiguities.joinToString()}", color = Color.Red)
    }
    if (interpretation.serviceSuggestion != null && !interpretation.serviceSuggestion.equals(serviceName, ignoreCase = true)) {
        Text("La IA sugirió otro servicio. Vuelve a servicios para cambiarlo y revisa el requerimiento.", color = Color.Red)
    }
}

private val FREQUENT_DIMENSIONS = listOf(
    "2 × 2" to (2.0 to 2.0), "2.5 × 2" to (2.5 to 2.0), "3 × 2" to (3.0 to 2.0),
    "3 × 4" to (3.0 to 4.0), "4 × 3" to (4.0 to 3.0), "4 × 4" to (4.0 to 4.0),
    "5 × 4" to (5.0 to 4.0), "6 × 3" to (6.0 to 3.0), "6 × 4" to (6.0 to 4.0),
    "6 × 5" to (6.0 to 5.0), "2 × 6" to (2.0 to 6.0), "6 × 6" to (6.0 to 6.0),
    "7 × 4" to (7.0 to 4.0)
)

@Composable
private fun DimensionFields(
    length: String,
    width: String,
    height: String,
    showStructureGuidance: Boolean,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onSelectPreset: (Double, Double) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(FREQUENT_DIMENSIONS) { (label, dimensions) ->
            val selected = length.toDoubleOrNull() == dimensions.first && width.toDoubleOrNull() == dimensions.second
            FilterChip(
                selected = selected,
                onClick = { onSelectPreset(dimensions.first, dimensions.second) },
                label = { Text("$label m") }
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = length,
            onValueChange = onLengthChange,
            label = { Text("Largo (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = width,
            onValueChange = onWidthChange,
            label = { Text("Ancho (m)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
    StructureGuidance(length, width, showStructureGuidance)
    OutlinedTextField(
        value = height,
        onValueChange = onHeightChange,
        label = { Text("Altura (m, opcional)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun StructureGuidance(length: String, width: String, visible: Boolean) {
    if (!visible) return
    val dimensions = listOfNotNull(length.replace(',', '.').toDoubleOrNull(), width.replace(',', '.').toDoubleOrNull())
    val largest = dimensions.maxOrNull() ?: return
    if (dimensions.size == 2 && dimensions.all { it > 0.0 }) {
        val structure = if (largest >= 4.0) "tubo galvanizado" else "tubo electro"
        Text("Estructura según la regla indicada: $structure.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MaterialFields(
    serviceOptions: List<String>,
    isMotorcycle: Boolean,
    value: String,
    onValueChange: (String) -> Unit
) {
    when {
        serviceOptions.isNotEmpty() -> MaterialOptions(serviceOptions, value, onValueChange)
        isMotorcycle -> MotorcycleMaterial(value, onValueChange)
        else -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Material mencionado (confirmar)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun MaterialOptions(options: List<String>, value: String, onValueChange: (String) -> Unit) {
    Text("Selecciona una opción o indica otra. El precio requiere una tarifa aprobada para el material exacto.", style = MaterialTheme.typography.bodySmall)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { option ->
            FilterChip(selected = value == option, onClick = { onValueChange(option) }, label = { Text(option) })
        }
        item { FilterChip(selected = value.isBlank(), onClick = { onValueChange("") }, label = { Text("Otro") }) }
    }
    if (value.isBlank()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Escribe el material") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
    Text("Las carpas se cotizan con lino económico, liviano o pesado. Los toldos usan Tarflex o lona según la opción elegida.", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun MotorcycleMaterial(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Material o acabado (según lo acordado)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Text("El tipo o modelo de moto se especifica abajo. Confirma material y tarifa con el jefe.", style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun TentFields(
    visible: Boolean,
    closed: Boolean,
    windows: String,
    doors: String,
    onClosedChange: (Boolean) -> Unit,
    onWindowsChange: (String) -> Unit,
    onDoorsChange: (String) -> Unit
) {
    if (!visible) return
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Características de la carpa", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = closed, onCheckedChange = onClosedChange)
                Text("Carpa cerrada")
            }
            if (closed) {
                OutlinedTextField(windows, onWindowsChange, label = { Text("Cantidad de ventanas") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(doors, onDoorsChange, label = { Text("Cantidad de puertas (inicia en 1)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        }
    }
}

@Composable
private fun MotorcycleField(isMotorcycle: Boolean, value: String, onValueChange: (String) -> Unit) {
    if (!isMotorcycle) return
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Tipo o modelo de moto") },
        placeholder = { Text("Ejemplo: lineal, scooter, marca y modelo") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

private data class QuoteFormInput(
    val serviceId: Int,
    val serviceName: String,
    val operator: String,
    val offline: Boolean,
    val requirement: String,
    val lengthText: String,
    val widthText: String,
    val heightText: String,
    val material: String,
    val motorcycleType: String,
    val isCarpas: Boolean,
    val isMoto: Boolean,
    val tentClosed: Boolean,
    val windowsText: String,
    val doorsText: String,
    val correctedByPerson: Boolean,
    val interpretation: RequirementInterpretation?
) {
    val length: Double? get() = lengthText.toPositiveDecimalOrNull()
    val width: Double? get() = widthText.toPositiveDecimalOrNull()
    val height: Double? get() = heightText.toPositiveDecimalOrNull()
    val windows: Int? get() = windowsText.toIntOrNull()
    val doors: Int? get() = doorsText.toIntOrNull()
}

private fun validateQuoteForm(input: QuoteFormInput): String? {
    if (input.requirement.isBlank()) return "Describa el requerimiento del cliente."
    val suggestedService = input.interpretation?.serviceSuggestion
    if (suggestedService != null && !suggestedService.equals(input.serviceName, ignoreCase = true)) {
        return "La IA sugirió $suggestedService. Vuelve a servicios, selecciona el correcto y revisa los datos."
    }
    if (input.lengthText.isBlank() || input.widthText.isBlank()) {
        return "Ingresa largo y ancho para guardar la cotización."
    }
    if (input.length == null || input.width == null) {
        return "Cada medida ingresada debe ser positiva y usar un decimal válido."
    }
    if (input.heightText.isNotBlank() && input.height == null) {
        return "La altura opcional debe ser un número mayor que cero."
    }
    if (input.isCarpas && input.tentClosed && (input.windows == null || input.doors?.let { it < 1 } != false)) {
        return "Indica cuántas ventanas necesita y registra al menos una puerta para la carpa cerrada."
    }
    return null
}

private fun QuoteFormInput.toCotizacion(): CotizacionEntity {
    val now = System.currentTimeMillis()
    return CotizacionEntity(
        id = 0,
        codigoCotizacion = "COT-$now",
        servicioId = serviceId,
        materialId = null,
        operador = operator,
        requerimientoCliente = requirement.trim(),
        interpretacionIaJson = interpretation?.toAuditJson(),
        corregidoPorHumano = correctedByPerson,
        medidaLargo = length,
        medidaAncho = width,
        medidaAlto = height,
        precioReferencial = 0.0,
        descuentoMonto = 0.0,
        descuentoPorcentaje = 0.0,
        precioFinal = 0.0,
        estado = "PENDIENTE",
        fechaHoraInicio = now,
        fechaHoraFin = null,
        esModoOffline = offline,
        tipoMoto = motorcycleType.trim().takeIf { isMoto && it.isNotBlank() },
        tipoCarpa = if (isCarpas) if (tentClosed) "CERRADA" else "ABIERTA" else null,
        cantidadVentanas = if (isCarpas && tentClosed) windows else null,
        cantidadPuertas = if (isCarpas && tentClosed) doors else null,
        materialDescripcion = material.trim().takeIf(String::isNotBlank)
    )
}

private fun RequirementInterpretation.toAuditJson(): String = JSONObject()
    .put("servicio_sugerido", serviceSuggestion ?: JSONObject.NULL)
    .put("largo_sugerido", dimensions["largo"] ?: JSONObject.NULL)
    .put("ancho_sugerido", dimensions["ancho"] ?: JSONObject.NULL)
    .put("caracteristicas_sugeridas", JSONObject().apply {
        characteristics.forEach { (key, value) -> put(key, value) }
    })
    .put("campos_faltantes", missingFields)
    .put("ambiguedades", ambiguities)
    .put("requiere_revision", reviewRequired)
    .put("revisada_por_persona", true)
    .toString()

private fun String.toPositiveDecimalOrNull(): Double? =
    replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
