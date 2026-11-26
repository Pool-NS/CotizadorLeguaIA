package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.CotizacionEntity

data class QuoteDurationMetrics(
    val sampleCount: Int,
    val averageMillis: Long?,
    val medianMillis: Long?
)

/** Summarizes nonnegative monotonic intervals from quote start to confirmed price communication. */
fun calculateQuoteDurationMetrics(quotes: List<CotizacionEntity>): QuoteDurationMetrics {
    val durations = quotes.mapNotNull { quote ->
        quote.durationMillis?.takeIf { it >= 0L }
    }.sorted()
    if (durations.isEmpty()) return QuoteDurationMetrics(0, null, null)

    val middle = durations.size / 2
    val median = if (durations.size % 2 == 1) {
        durations[middle]
    } else {
        (durations[middle - 1] / 2.0 + durations[middle] / 2.0).toLong()
    }
    return QuoteDurationMetrics(
        sampleCount = durations.size,
        averageMillis = durations.average().toLong(),
        medianMillis = median
    )
}
