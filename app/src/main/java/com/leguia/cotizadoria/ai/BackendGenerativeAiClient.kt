package com.leguia.cotizadoria.ai

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.SocketTimeoutException
import kotlinx.coroutines.CancellationException
import java.io.ByteArrayOutputStream
import com.leguia.cotizadoria.firebase.NoOpTraceRecorder
import com.leguia.cotizadoria.firebase.TraceRecorder

/** Calls the private project backend; provider credentials never enter the Android app. */
class BackendGenerativeAiClient(
    private val baseUrl: String,
    private val ioDispatcher: CoroutineDispatcher,
    private val traceRecorder: TraceRecorder = NoOpTraceRecorder
) : GenerativeAiClient {
    override suspend fun interpret(requirement: String): Result<RequirementInterpretation> =
        withContext(ioDispatcher) {
            val trace = traceRecorder.start("ai_interpretation")
            try {
            val interpretation = try {
                require(requirement.isNotBlank()) { "Escribe o dicta un requerimiento." }
                require(baseUrl.isNotBlank()) { "Configura la URL del backend de IA." }
                val endpoint = URI.create("${baseUrl.trimEnd('/')}/v1/interpret").toURL()
                val connection = endpoint.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "POST"
                    connection.connectTimeout = 8_000
                    connection.readTimeout = 30_000
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connection.setRequestProperty("ngrok-skip-browser-warning", "true")
                    connection.outputStream.use { output ->
                        output.write(JSONObject().put("requirement", requirement).toString().toByteArray(Charsets.UTF_8))
                    }
                    val status = connection.responseCode
                    if (status !in 200..299) {
                        throw AiServiceException(status)
                    }
                    val response = connection.readBoundedResponse()
                    response.toInterpretation()
                } finally {
                    connection.disconnect()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                return@withContext Result.failure(IOException(failure.toSafeUserMessage()))
            }
            Result.success(interpretation)
            } finally {
                trace.stop()
            }
        }
}

private const val MAX_RESPONSE_CHARACTERS = 32_000

private fun HttpURLConnection.readBoundedResponse(): String = inputStream.use { input ->
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(4_096)
    var totalBytes = 0
    while (true) {
        val count = input.read(buffer)
        if (count < 0) break
        totalBytes += count
        require(totalBytes <= MAX_RESPONSE_CHARACTERS) { "Respuesta de IA demasiado extensa." }
        output.write(buffer, 0, count)
    }
    String(output.toByteArray(), Charsets.UTF_8)
}

private class AiServiceException(val statusCode: Int) : IOException()

internal fun Exception.toSafeUserMessage(): String = when (this) {
    is AiServiceException -> when (statusCode) {
        400 -> "No se pudo interpretar la solicitud. Revisa el texto e inténtalo otra vez."
        429 -> "El servicio de IA está temporalmente ocupado. Inténtalo más tarde o completa los datos manualmente."
        in 500..599 -> "El servicio de IA no está disponible por el momento. Puedes reintentar o completar los datos manualmente."
        else -> "No se pudo completar la interpretación. Puedes completar los datos manualmente."
    }
    is SocketTimeoutException -> "La interpretación tardó demasiado. Reintenta o completa los datos manualmente."
    is IllegalArgumentException -> "La respuesta de interpretación no fue válida. Puedes completar los datos manualmente."
    else -> "No pudimos conectar con el servicio de IA. Reintenta o completa los datos manualmente."
}

private fun String.toInterpretation(): RequirementInterpretation {
    val json = JSONObject(this)
    val dimensions = buildMap {
        json.optNullableDouble("length_m")?.let { put("largo", it) }
        json.optNullableDouble("width_m")?.let { put("ancho", it) }
    }
    require(dimensions.values.all { it > 0.0 })
    val service = json.optNullableString("service")
    require(service == null || service in ALLOWED_SERVICES)
    val characteristics = buildMap {
        putOptional(json, "material", "material")
        putOptional(json, "motorcycle_type", "tipoMoto")
        putOptional(json, "tent_type", "tipoCarpa")
        putOptional(json, "windows", "ventanas")
        putOptional(json, "doors", "puertas")
    }
    return RequirementInterpretation(
        serviceSuggestion = service,
        dimensions = dimensions,
        characteristics = characteristics,
        missingFields = json.optStringArray("missing_fields"),
        ambiguities = json.optStringArray("ambiguities"),
        reviewRequired = true
    )
}

private val ALLOWED_SERVICES = setOf("Carpas", "Toldos", "Tapizado de moto")

private fun JSONObject.optNullableDouble(name: String): Double? =
    if (isNull(name) || !has(name)) null else optDouble(name).takeIf(Double::isFinite)

private fun JSONObject.optNullableString(name: String): String? =
    if (isNull(name) || !has(name)) null else optString(name).trim().takeIf(String::isNotEmpty)?.also {
        require(it.length <= 100 && it.none(Char::isISOControl))
    }

private fun JSONObject.optStringArray(name: String): List<String> =
    optJSONArray(name)?.let { values ->
        require(values.length() <= 20)
        (0 until values.length()).mapNotNull { index -> values.optString(index).trim().takeIf(String::isNotEmpty)?.also {
            require(it.length <= 160 && it.none(Char::isISOControl))
        } }
    }.orEmpty()

private fun MutableMap<String, String>.putOptional(json: JSONObject, field: String, key: String) {
    val value = if (json.isNull(field)) null else json.optString(field).trim().takeIf(String::isNotEmpty)?.also {
        require(it.length <= 100 && it.none(Char::isISOControl))
        if (field == "tent_type") require(it.equals("ABIERTA", true) || it.equals("CERRADA", true))
        if (field == "windows" || field == "doors") require(it.toIntOrNull()?.let { count -> count in 0..99 } == true)
    }
    value?.let { put(key, it) }
}
