package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.StockMaterialRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MaterialReadinessTest {
    @Test
    fun realMaterialWithoutConsumptionRuleRequiresHumanReview() {
        val result = MaterialReadinessEvaluator.assess(
            serviceName = "Carpas",
            materialName = "Lona PVC",
            lengthM = 3.0,
            widthM = 2.0,
            tentType = "ABIERTA",
            inventory = listOf(StockMaterialRow("Lona PVC", 50.0, "m²"))
        )

        assertEquals(MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED, result.status)
        assertNull(result.shortage)
    }

    @Test
    fun demoScenarioCalculatesShortageWithoutChangingStock() {
        val result = MaterialReadinessEvaluator.assess(
            serviceName = "Carpas",
            materialName = "Lino pesado DEMO",
            lengthM = 4.0,
            widthM = 3.0,
            tentType = "CERRADA",
            inventory = listOf(StockMaterialRow("Lino pesado DEMO", 10.0, "m² simulado"))
        )

        assertEquals(MaterialReadinessStatus.SHORTAGE, result.status)
        assertEquals(18.6, result.required!!, 0.0001)
        assertEquals(8.6, result.shortage!!, 0.0001)
        assertEquals(10.0, result.available!!, 0.0)
    }

    @Test
    fun demoScenarioReportsSufficientStockWhenQuantityCoversRequirement() {
        val result = MaterialReadinessEvaluator.assess(
            serviceName = "Toldos",
            materialName = "Tarflex DEMO",
            lengthM = 3.0,
            widthM = 2.0,
            tentType = null,
            inventory = listOf(StockMaterialRow("Tarflex DEMO", 24.0, "m² simulado"))
        )

        assertEquals(MaterialReadinessStatus.SUFFICIENT_SYNTHETIC_SCENARIO, result.status)
        assertEquals(6.6, result.required!!, 0.0001)
        assertEquals(0.0, result.shortage!!, 0.0)
    }
}
