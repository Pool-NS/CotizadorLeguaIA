package com.leguia.cotizadoria.domain

/** Domain rules for manual inventory movements; unit conversion is intentionally not inferred. */
object StockMovementRules {
    fun signedDelta(type: String, amount: Double): Double {
        require(amount.isFinite() && amount != 0.0) { "Ingresa un cambio distinto de cero." }
        return when (type) {
            "SALIDA" -> {
                require(amount > 0.0) { "La salida debe ingresarse como una cantidad positiva." }
                -amount
            }
            "AJUSTE" -> amount
            else -> error("Tipo de movimiento no permitido.")
        }
    }

    fun resultingBalance(current: Double, delta: Double): Double {
        require(current.isFinite() && current >= 0.0 && delta.isFinite()) { "Saldo o cambio no válido." }
        val result = current + delta
        require(result.isFinite() && result >= 0.0) { "La operación dejaría un saldo negativo. Revisa la cantidad." }
        return result
    }

    fun requireSameUnit(current: String, incoming: String) {
        require(current.trim().equals(incoming.trim(), ignoreCase = true)) {
            "La unidad no coincide con la registrada. No se convierten unidades automáticamente."
        }
    }
}
