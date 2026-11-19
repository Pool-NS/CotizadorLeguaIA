package com.leguia.cotizadoria.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StockMovementRulesTest {
    @Test
    fun salidaConCantidadPositivaSeRegistraComoCambioNegativo() {
        assertEquals(-2.5, StockMovementRules.signedDelta("SALIDA", 2.5), 0.0)
    }

    @Test
    fun ajusteAceptaCambioFirmado() {
        assertEquals(3.0, StockMovementRules.signedDelta("AJUSTE", 3.0), 0.0)
        assertEquals(-3.0, StockMovementRules.signedDelta("AJUSTE", -3.0), 0.0)
    }

    @Test
    fun noPermiteSaldoNegativoNiCambioNoFinito() {
        assertThrows(IllegalArgumentException::class.java) {
            StockMovementRules.resultingBalance(1.0, -1.1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            StockMovementRules.signedDelta("SALIDA", Double.NaN)
        }
    }

    @Test
    fun noConvierteUnidadesDeFormaImplicita() {
        StockMovementRules.requireSameUnit("m", " M ")
        assertThrows(IllegalArgumentException::class.java) {
            StockMovementRules.requireSameUnit("m", "m²")
        }
    }
}
