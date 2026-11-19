package com.leguia.cotizadoria.domain

import com.leguia.cotizadoria.data.StockMaterialRow

enum class MaterialReadinessStatus {
    MATERIAL_MISSING,
    STOCK_NOT_REGISTERED,
    CONSUMPTION_RULE_REQUIRED,
    REQUIREMENT_INCOMPLETE
}

data class MaterialReadinessResult(
    val status: MaterialReadinessStatus,
    val material: String?,
    val available: Double? = null,
    val unit: String? = null
)

/** Reports known inventory only; it never invents consumption or reserves stock. */
object MaterialReadinessEvaluator {
    fun assess(
        materialName: String?,
        lengthM: Double?,
        widthM: Double?,
        inventory: List<StockMaterialRow>
    ): MaterialReadinessResult {
        val material = materialName?.trim()?.takeIf(String::isNotBlank)
            ?: return MaterialReadinessResult(MaterialReadinessStatus.MATERIAL_MISSING, null)

        if (lengthM == null || widthM == null || !lengthM.isFinite() || !widthM.isFinite() || lengthM <= 0.0 || widthM <= 0.0) {
            return MaterialReadinessResult(MaterialReadinessStatus.REQUIREMENT_INCOMPLETE, material)
        }

        val stock = inventory.firstOrNull { it.nombreMaterial.equals(material, ignoreCase = true) }
            ?: return MaterialReadinessResult(MaterialReadinessStatus.STOCK_NOT_REGISTERED, material)

        return MaterialReadinessResult(
            status = MaterialReadinessStatus.CONSUMPTION_RULE_REQUIRED,
            material = material,
            available = stock.cantidadDisponible,
            unit = stock.unidad
        )
    }
}
