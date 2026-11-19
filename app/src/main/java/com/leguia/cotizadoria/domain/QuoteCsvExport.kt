package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.CotizacionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Exports operational evidence while omitting client text and operator identity. */
object QuoteCsvExport {
    private val header = listOf(
        "record_id",
        "observation_date_utc",
        "start_timestamp_utc",
        "price_communication_timestamp_utc",
        "phase_pre_post",
        "service_type",
        "attention_modality",
        "record_source",
        "connection_mode",
        "software_version",
        "quotation_time_minutes",
        "operator_confirmed_price_communication",
        "quote_correct_independent_audit",
        "attributable_error_count",
        "attributable_error_codes",
        "ai_fields_corrected_by_operator"
    )

    fun build(
        quotes: List<CotizacionEntity>,
        serviceNames: Map<Int, String>,
        softwareVersion: String
    ): String {
        val rows = quotes.sortedBy(CotizacionEntity::fechaHoraInicio).map { quote ->
            val startTimestamp = formatUtc(quote.fechaHoraInicio)
            val communicationTimestamp = quote.fechaHoraPrecioComunicado?.let(::formatUtc).orEmpty()
            val durationMinutes = quote.fechaHoraPrecioComunicado
                ?.minus(quote.fechaHoraInicio)
                ?.takeIf { it >= 0L }
                ?.div(60_000.0)
                ?.let { String.format(Locale.ROOT, "%.4f", it) }
                .orEmpty()
            listOf(
                "OBS-${quote.id.toString().padStart(6, '0')}",
                startTimestamp.substringBefore('T'),
                startTimestamp,
                communicationTimestamp,
                "", // The approved pre/post protocol assigns the phase.
                serviceNames[quote.servicioId].orEmpty(),
                "", // The observer records the approved attention modality.
                "ANDROID_ROOM",
                if (quote.esModoOffline) "offline" else "online",
                softwareVersion,
                durationMinutes,
                if (!quote.capturaComunicacionCompleta) "" else if (communicationTimestamp.isNotEmpty()) "1" else "0",
                "", // Accuracy requires an independent verification instrument.
                "", // Attributable errors require the approved manual observation.
                "",
                if (quote.interpretacionIaJson == null) "" else if (quote.corregidoPorHumano) "1" else "0"
            )
        }
        return (listOf(header) + rows).joinToString(separator = "\r\n") { row ->
            row.joinToString(separator = ",", transform = ::escapeCell)
        } + "\r\n"
    }

    private fun formatUtc(timestamp: Long): String = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        Locale.ROOT
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(timestamp))

    private fun escapeCell(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"$escaped\"" else escaped
    }
}
