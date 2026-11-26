package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.CotizacionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteCsvExportTest {
    @Test
    fun exportUsesStableIdAndVersionWithoutCustomerTextOrOperator() {
        val csv = QuoteCsvExport.build(listOf(sampleQuote()), mapOf(1 to "Carpas"), "1.5.1")

        assertTrue(csv.contains("record_id,observation_date_utc,start_timestamp_utc"))
        assertTrue(csv.contains("OBS-000042,1970-01-01,1970-01-01T00:00:01.000Z,1970-01-01T00:01:31.000Z"))
        assertTrue(csv.contains(",Carpas,,ANDROID_ROOM,ai_assisted,1.5.1,1.5000,90000,1,,,,,"))
        assertFalse(csv.contains("cliente confidencial"))
        assertFalse(csv.contains("Operador Piero"))
        assertFalse(csv.contains("COT-12345"))
    }

    @Test
    fun unverifiedAccuracyAndPhaseStayBlank() {
        val csv = QuoteCsvExport.build(listOf(sampleQuote().copy(capturaComunicacionCompleta = false, durationMillis = null)), emptyMap(), "1.5.1")
        val row = csv.trim().lines().last()

        val cells = row.split(',')
        assertEquals("", cells[4])
        assertEquals("", cells[12])
        assertEquals("", cells[13])
        assertEquals("", cells[14])
        assertEquals("", cells[15])
        assertTrue(cells[17].isNotBlank())
        assertEquals("LOCAL_ONLY", cells[18])
    }

    @Test
    fun independentAccuracyAndErrorsRemainUnmeasuredEvenAfterAiCorrection() {
        val quote = sampleQuote().copy(
            interpretacionIaJson = "{}",
            corregidoPorHumano = true
        )
        val cells = QuoteCsvExport.build(listOf(quote), emptyMap(), "1.5.1").trim().lines().last().split(',')

        assertEquals("", cells[13])
        assertEquals("", cells[14])
        assertEquals("1", cells[16])
        assertTrue(cells[17].isNotBlank())
    }

    private fun sampleQuote() = CotizacionEntity(
        id = 42,
        codigoCotizacion = "COT-12345",
        servicioId = 1,
        materialId = null,
        operador = "Operador Piero",
        requerimientoCliente = "cliente confidencial",
        interpretacionIaJson = null,
        corregidoPorHumano = false,
        medidaLargo = 3.0,
        medidaAncho = 2.0,
        medidaAlto = null,
        precioReferencial = 200.0,
        descuentoMonto = 0.0,
        descuentoPorcentaje = 0.0,
        precioFinal = 200.0,
        estado = "PENDIENTE",
        fechaHoraInicio = 1_000L,
        fechaHoraFin = null,
        esModoOffline = false,
        fechaHoraPrecioComunicado = 91_000L,
        capturaComunicacionCompleta = true,
        durationMillis = 90_000L
    )
}
