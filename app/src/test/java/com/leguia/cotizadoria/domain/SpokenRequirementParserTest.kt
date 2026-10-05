package com.leguia.cotizadoria.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpokenRequirementParserTest {
    @Test fun detectsServiceAndLabelledMeasurements() {
        val result = SpokenRequirementParser.interpret("Necesito una carpa, largo 3 metros y ancho 2 metros")
        assertEquals("Carpas", result.service)
        assertEquals(3.0, result.largo!!, 0.0)
        assertEquals(2.0, result.ancho!!, 0.0)
    }

    @Test fun ignoresHeightAndAcceptsSpokenDecimalForWidth() {
        val result = SpokenRequirementParser.interpret("un toldo de ancho dos punto cinco y altura uno")
        assertEquals("Toldos", result.service)
        assertEquals(2.5, result.ancho!!, 0.0)
    }

    @Test fun ambiguousServiceRequiresManualSelection() {
        val result = SpokenRequirementParser.interpret("una carpa y un toldo")
        assertNull(result.service)
        assertEquals(listOf("Carpas", "Toldos"), result.ambiguousServices)
    }

    @Test fun repeatedConflictingDimensionsAreNotChosenSilently() {
        val result = SpokenRequirementParser.interpret("carpa largo 2 y largo 3")
        assertNull(result.largo)
        assertEquals(listOf("largo"), result.dimensionsNeedingReview)
    }

    @Test fun parsesCompactThreeByTwoAsLengthAndWidth() {
        val result = SpokenRequirementParser.interpret("una carpa de 3 x2")
        assertEquals(3.0, result.largo!!, 0.0)
        assertEquals(2.0, result.ancho!!, 0.0)
    }

    @Test fun parsesSpokenThreeByTwoAndKeepsHeightOptional() {
        val result = SpokenRequirementParser.interpret("una carpa de tres por dos")
        assertEquals(3.0, result.largo!!, 0.0)
        assertEquals(2.0, result.ancho!!, 0.0)
    }

    @Test fun recognizesMotorcycleUpholsteryService() {
        val result = SpokenRequirementParser.interpret("cotizar tapizado de moto lineal")
        assertEquals("Tapizado de moto", result.service)
    }

}
