package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.StockMaterialRow
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterialReadinessTest {
    @Test
    fun registeredMaterialWithoutValidatedConsumptionRuleRequiresHumanReview() {
        val result = MaterialReadinessEvaluator.assess(
            materialName = "Lona PVC",
            lengthM = 3.0,
            widthM = 2.0,
            inventory = listOf(StockMaterialRow("Lona PVC", 50.0, "m²"))
        )

        assertEquals(MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED, result.status)
        assertEquals(50.0, result.available!!, 0.0)
    }

    @Test
    fun demoNamedMaterialNeverUsesAnInventedConsumptionRule() {
        val result = MaterialReadinessEvaluator.assess(
            materialName = "Lino pesado DEMO",
            lengthM = 4.0,
            widthM = 3.0,
            inventory = listOf(StockMaterialRow("Lino pesado DEMO", 10.0, "m² simulado"))
        )

        assertEquals(MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED, result.status)
        assertEquals(10.0, result.available!!, 0.0)
    }

    @Test
    fun demoNamedMaterialDoesNotClaimSufficientStock() {
        val result = MaterialReadinessEvaluator.assess(
            materialName = "Tarflex DEMO",
            lengthM = 3.0,
            widthM = 2.0,
            inventory = listOf(StockMaterialRow("Tarflex DEMO", 24.0, "m² simulado"))
        )

        assertEquals(MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED, result.status)
    }
}
