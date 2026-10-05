package com.leguia.cotizadoria.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Calls the private project backend; provider credentials never enter the Android app. */
class BackendGenerativeAiClient(private val baseUrl: String) : GenerativeAiClient {
    override suspend fun interpret(requirement: String): Result<RequirementInterpretation> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(requirement.isNotBlank()) { "Escribe o dicta un requerimiento." }
                require(baseUrl.isNotBlank()) { "Configura la URL del backend de IA." }
                val connection = (URL("${baseUrl.trimEnd('/')}/v1/interpret").openConnection() as HttpURLConnection)
                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 8_000
                    connection.readTimeout = 30_000
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connection.outputStream.use { output ->
                        output.write(JSONObject().put("requirement", requirement).toString().toByteArray(Charsets.UTF_8))
                    }
                    val status = connection.responseCode
                    if (status !in 200..299) {
                        throw IOException("El servicio de interpretación no está disponible (HTTP $status).")
                    }
                    val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    response.toInterpretation()
                } finally {
                    connection.disconnect()
                }
            }
        }
}

private fun String.toInterpretation(): RequirementInterpretation {
    val json = JSONObject(this)
    val dimensions = buildMap {
        json.optNullableDouble("length_m")?.let { put("largo", it) }
        json.optNullableDouble("width_m")?.let { put("ancho", it) }
    }
    val characteristics = buildMap {
        putOptional(json, "material", "material")
        putOptional(json, "motorcycle_type", "tipoMoto")
        putOptional(json, "tent_type", "tipoCarpa")
        putOptional(json, "windows", "ventanas")
        putOptional(json, "doors", "puertas")
    }
    return RequirementInterpretation(
        serviceSuggestion = json.optNullableString("service"),
        dimensions = dimensions,
        characteristics = characteristics,
        missingFields = json.optStringArray("missing_fields"),
        ambiguities = json.optStringArray("ambiguities"),
        reviewRequired = true
    )
}

private fun JSONObject.optNullableDouble(name: String): Double? =
    if (isNull(name) || !has(name)) null else optDouble(name).takeIf(Double::isFinite)

private fun JSONObject.optNullableString(name: String): String? =
    if (isNull(name) || !has(name)) null else optString(name).trim().takeIf(String::isNotEmpty)

private fun JSONObject.optStringArray(name: String): List<String> =
    optJSONArray(name)?.let { values ->
        (0 until values.length()).mapNotNull { index -> values.optString(index).trim().takeIf(String::isNotEmpty) }
    }.orEmpty()

private fun MutableMap<String, String>.putOptional(json: JSONObject, field: String, key: String) {
    val value = if (json.isNull(field)) null else json.optString(field).trim().takeIf(String::isNotEmpty)
    value?.let { put(key, it) }
}
