package com.leguia.cotizadoria.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PricingWorkflowTest {
    @Test fun configuredParametersCalculateDeterministically() {
        val result = QuotePricingCalculator.calculate(
            listOf(PriceLine(ConfiguredPrice(10.0, "unit"), 3.0)), humanDiscount = 5.0
        )
        assertEquals(PricingOutcome.Calculated(PriceResult(30.0, 5.0, 25.0)), result)
    }

    @Test fun sizePriceKeysSeparateMaterialTentTypeAndDimensions() {
        val openThreeByTwo = DimensionPriceKey.forSize(3.0, 2.0, "ABIERTA", "Lona")
        val closedThreeByTwo = DimensionPriceKey.forSize(3.0, 2.0, "CERRADA", "Lona")
        val openTwoByTwo = DimensionPriceKey.forSize(2.0, 2.0, "ABIERTA", "Lona")
        val oxfordThreeByTwo = DimensionPriceKey.forSize(3.0, 2.0, "ABIERTA", "Oxford")
        assertTrue(openThreeByTwo != closedThreeByTwo)
        assertTrue(openThreeByTwo != openTwoByTwo)
        assertTrue(openThreeByTwo != oxfordThreeByTwo)
        assertEquals(openThreeByTwo, DimensionPriceKey.forSize(3.0, 2.0, "ABIERTA", "LÓNA"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun materialIsRequiredForApprovedPriceKey() {
        DimensionPriceKey.forSize(3.0, 2.0, "BASE", " ")
    }

    @Test fun missingParametersNeverInventAPrice() {
        assertTrue(QuotePricingCalculator.calculate(emptyList(), 0.0) is PricingOutcome.NeedsConfiguration)
    }

    @Test fun invalidQuantityIsRejected() {
        assertTrue(QuotePricingCalculator.calculate(listOf(PriceLine(ConfiguredPrice(4.0, "m"), -1.0)), 0.0) is PricingOutcome.NeedsConfiguration)
    }

    @Test fun onlyPendingQuotesCanReachCommercialOutcome() {
        assertTrue(QuoteWorkflow.transition(QuoteStatus.PENDIENTE, QuoteStatus.CONCRETADA, "manager", 1).isSuccess)
        assertTrue(QuoteWorkflow.transition(QuoteStatus.CONCRETADA, QuoteStatus.NO_CONCRETADA, "manager", 2).isFailure)
    }

    @Test fun impactSimulationIsReadOnlyAndRequiresKnownInputs() {
        assertEquals(Feasibility.VIABLE_CON_ABASTECIMIENTO, ImpactSimulator.assess(ImpactInput(true, true, true, 2)))
        assertEquals(Feasibility.SIN_INFORMACION_SUFICIENTE, ImpactSimulator.assess(ImpactInput(true, true, false, 0)))
    }

    @Test fun materialShortageIsReportedWithoutReservingStock() {
        assertEquals(3.0, MaterialAvailabilityCalculator.calculate(8.0, 5.0).shortage!!, 0.0)
        assertEquals(null, MaterialAvailabilityCalculator.calculate(null, 5.0).shortage)
    }
}
