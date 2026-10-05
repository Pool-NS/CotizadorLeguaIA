package com.leguia.cotizadoria.ai

data class RequirementInterpretation(
    val serviceSuggestion: String?,
    val dimensions: Map<String, Double>,
    val characteristics: Map<String, String>,
    val missingFields: List<String> = emptyList(),
    val ambiguities: List<String> = emptyList(),
    val reviewRequired: Boolean = true
)

/** Implementations may suggest structure only; a person reviews and confirms all fields. */
interface GenerativeAiClient {
    suspend fun interpret(requirement: String): Result<RequirementInterpretation>
}

/** Deterministic test double. It does not infer or fabricate business facts. */
class FakeGenerativeAiClient : GenerativeAiClient {
    override suspend fun interpret(requirement: String): Result<RequirementInterpretation> =
        if (requirement.isBlank()) Result.failure(IllegalArgumentException("Requirement is empty"))
        else Result.success(RequirementInterpretation(null, emptyMap(), emptyMap(), reviewRequired = true))
}
