package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.StockMaterialRow
import java.text.Normalizer
import java.util.Locale

enum class MaterialReadinessStatus {
    MATERIAL_MISSING,
    STOCK_NOT_REGISTERED,
    CONSUMPTION_RULE_REQUIRED,
    REQUIREMENT_INCOMPLETE,
    SHORTAGE,
    SUFFICIENT_SYNTHETIC_SCENARIO
}

data class MaterialReadinessResult(
    val status: MaterialReadinessStatus,
    val material: String?,
    val required: Double? = null,
    val available: Double? = null,
    val shortage: Double? = null,
    val unit: String? = null,
    val synthetic: Boolean = false
)

/** Stock is never reserved or reduced by this read-only assessment. */
object MaterialReadinessEvaluator {
    private data class SyntheticRule(val factor: Double, val unit: String)

    private val syntheticRules = mapOf(
        "lona pvc demo" to SyntheticRule(1.12, "m² simulados"),
        "tarflex demo" to SyntheticRule(1.10, "m² simulados"),
        "oxford demo" to SyntheticRule(0.55, "m² equivalentes DEMO")
    )

    fun assess(
        serviceName: String,
        materialName: String?,
        lengthM: Double?,
        widthM: Double?,
        tentType: String?,
        inventory: List<StockMaterialRow>
    ): MaterialReadinessResult {
        val material = materialName?.trim()?.takeIf(String::isNotBlank)
            ?: return MaterialReadinessResult(MaterialReadinessStatus.MATERIAL_MISSING, null)
        val stock = inventory.firstOrNull { normalize(it.nombreMaterial) == normalize(material) }
            ?: return MaterialReadinessResult(MaterialReadinessStatus.STOCK_NOT_REGISTERED, material)

        val rule = syntheticRules[normalize(material)]
        if (rule == null) {
            return MaterialReadinessResult(
                status = MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED,
                material = material,
                available = stock.cantidadDisponible,
                unit = stock.unidad
            )
        }
        if (lengthM == null || widthM == null || !lengthM.isFinite() || !widthM.isFinite() || lengthM <= 0 || widthM <= 0) {
            return MaterialReadinessResult(
                status = MaterialReadinessStatus.REQUIREMENT_INCOMPLETE,
                material = material,
                available = stock.cantidadDisponible,
                unit = stock.unidad,
                synthetic = true
            )
        }

        val normalizedService = normalize(serviceName)
        val factor = if (normalizedService == "carpas" && normalize(tentType.orEmpty()) == "cerrada") 1.55 else rule.factor
        val required = lengthM * widthM * factor
        val shortage = (required - stock.cantidadDisponible).coerceAtLeast(0.0)
        return MaterialReadinessResult(
            status = if (shortage > 0.0) MaterialReadinessStatus.SHORTAGE else MaterialReadinessStatus.SUFFICIENT_SYNTHETIC_SCENARIO,
            material = material,
            required = required,
            available = stock.cantidadDisponible,
            shortage = shortage,
            unit = rule.unit,
            synthetic = true
        )
    }

    private fun normalize(value: String): String = Normalizer.normalize(value.trim().lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
}
