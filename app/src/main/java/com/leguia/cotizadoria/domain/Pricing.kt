package com.leguia.cotizadoria.domain

import java.math.BigDecimal
import java.text.Normalizer
import java.util.Locale

/** A configured unit price; it must come from a business-approved source. */
data class ConfiguredPrice(val unitPrice: Double, val unit: String, val enabled: Boolean = true)

data class PriceLine(val configuredPrice: ConfiguredPrice, val quantity: Double)

data class PriceResult(val reference: Double, val humanDiscount: Double, val final: Double)

sealed interface PricingOutcome {
    data class Calculated(val result: PriceResult) : PricingOutcome
    data class NeedsConfiguration(val reason: String) : PricingOutcome
}

/** Deterministic arithmetic only; it does not infer units, quantities, or commercial rules. */
object QuotePricingCalculator {
    fun calculate(lines: List<PriceLine>, humanDiscount: Double): PricingOutcome {
        if (lines.isEmpty()) return PricingOutcome.NeedsConfiguration("No configured price lines")
        if (lines.any { !it.configuredPrice.enabled || it.configuredPrice.unit.isBlank() }) {
            return PricingOutcome.NeedsConfiguration("A price parameter is inactive or has no unit")
        }
        if (lines.any { !it.configuredPrice.unitPrice.isFinite() || it.configuredPrice.unitPrice < 0.0 || !it.quantity.isFinite() || it.quantity < 0.0 }) {
            return PricingOutcome.NeedsConfiguration("Price and quantity must be finite and non-negative")
        }
        if (!humanDiscount.isFinite() || humanDiscount < 0.0) {
            return PricingOutcome.NeedsConfiguration("Human discount must be finite and non-negative")
        }
        val reference = lines.sumOf { it.configuredPrice.unitPrice * it.quantity }
        if (!reference.isFinite() || humanDiscount > reference) {
            return PricingOutcome.NeedsConfiguration("Discount exceeds the configured reference price")
        }
        return PricingOutcome.Calculated(PriceResult(reference, humanDiscount, reference - humanDiscount))
    }
}

object DimensionPriceKey {
    fun forSize(length: Double, width: Double, variant: String, material: String): String {
        require(length.isFinite() && length > 0.0 && width.isFinite() && width > 0.0)
        val normalizedMaterial = normalize(material)
        require(normalizedMaterial.isNotBlank()) { "Material is required for an approved tariff" }
        val normalizedVariant = normalize(variant).ifBlank { "BASE" }
        return "MEDIDA:$normalizedVariant:$normalizedMaterial:${format(length)}x${format(width)}"
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.trim().uppercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("[^A-Z0-9_-]+"), "_")
            .trim('_')

    private fun format(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
}
