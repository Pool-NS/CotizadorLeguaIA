package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.CotizacionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuoteDurationMetricsTest {
    @Test
    fun calculatesAverageAndMedianForEvenSample() {
        val metrics = calculateQuoteDurationMetrics(listOf(quote(60_000L), quote(120_000L)))

        assertEquals(2, metrics.sampleCount)
        assertEquals(90_000L, metrics.averageMillis)
        assertEquals(90_000L, metrics.medianMillis)
    }

    @Test
    fun ignoresMissingAndNegativeDurations() {
        val metrics = calculateQuoteDurationMetrics(
            listOf(quote(60_000L), quote(null), quote(-1L).copy(fechaHoraInicio = 60_000L))
        )

        assertEquals(1, metrics.sampleCount)
        assertEquals(60_000L, metrics.averageMillis)
        assertEquals(60_000L, metrics.medianMillis)
    }

    @Test
    fun returnsEmptyMetricsWhenNoValidIntervalsExist() {
        val metrics = calculateQuoteDurationMetrics(listOf(quote(null)))

        assertEquals(0, metrics.sampleCount)
        assertNull(metrics.averageMillis)
        assertNull(metrics.medianMillis)
    }

    private fun quote(durationMillis: Long?, startMillis: Long = 0L) = CotizacionEntity(
        codigoCotizacion = "COT-$startMillis-$durationMillis",
        servicioId = 1,
        materialId = null,
        operador = "Operador",
        requerimientoCliente = "Carpa 3 x 2",
        interpretacionIaJson = null,
        corregidoPorHumano = false,
        medidaLargo = 3.0,
        medidaAncho = 2.0,
        medidaAlto = null,
        precioReferencial = 100.0,
        descuentoMonto = 0.0,
        descuentoPorcentaje = 0.0,
        precioFinal = 100.0,
        estado = "PENDIENTE",
        fechaHoraInicio = startMillis,
        fechaHoraFin = null,
        esModoOffline = false,
        fechaHoraPrecioComunicado = durationMillis?.let { startMillis + it },
        durationMillis = durationMillis
    )
}
