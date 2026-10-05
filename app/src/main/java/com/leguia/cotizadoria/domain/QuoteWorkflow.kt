package com.leguia.cotizadoria.domain

enum class QuoteStatus { PENDIENTE, NO_CONCRETADA, CONCRETADA }

data class StatusChange(val from: QuoteStatus, val to: QuoteStatus, val actor: String, val atMillis: Long, val note: String?)

object QuoteWorkflow {
    fun transition(from: QuoteStatus, to: QuoteStatus, actor: String, atMillis: Long, note: String? = null): Result<StatusChange> {
        if (actor.isBlank()) return Result.failure(IllegalArgumentException("Actor is required"))
        val allowed = from == QuoteStatus.PENDIENTE && to in setOf(QuoteStatus.CONCRETADA, QuoteStatus.NO_CONCRETADA)
        if (!allowed) return Result.failure(IllegalStateException("Unsupported quote status transition"))
        return Result.success(StatusChange(from, to, actor.trim(), atMillis, note))
    }
}

enum class Feasibility { VIABLE, VIABLE_CON_ABASTECIMIENTO, REVISAR_PROGRAMACION, SIN_INFORMACION_SUFICIENTE }

data class ImpactInput(
    val scheduleCapacityKnown: Boolean,
    val scheduleAvailable: Boolean,
    val materialDataKnown: Boolean,
    val shortages: Int
)

/** Read-only estimate. It deliberately has no inventory mutation or purchasing API. */
object ImpactSimulator {
    fun assess(input: ImpactInput): Feasibility = when {
        !input.scheduleCapacityKnown || !input.materialDataKnown || input.shortages < 0 -> Feasibility.SIN_INFORMACION_SUFICIENTE
        !input.scheduleAvailable -> Feasibility.REVISAR_PROGRAMACION
        input.shortages > 0 -> Feasibility.VIABLE_CON_ABASTECIMIENTO
        else -> Feasibility.VIABLE
    }
}

data class MaterialAvailability(val required: Double, val available: Double, val shortage: Double?)

object MaterialAvailabilityCalculator {
    /** Null shortage means that a required quantity or stock balance is not known. */
    fun calculate(required: Double?, available: Double?): MaterialAvailability {
        if (required == null || available == null || !required.isFinite() || !available.isFinite() || required < 0.0 || available < 0.0) {
            return MaterialAvailability(required ?: 0.0, available ?: 0.0, null)
        }
        return MaterialAvailability(required, available, (required - available).coerceAtLeast(0.0))
    }
}
