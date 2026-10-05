package com.leguia.cotizadoria.domain

import java.text.Normalizer
import java.util.Locale

data class SpokenRequirement(
    val service: String?,
    val ambiguousServices: List<String>,
    val largo: Double?,
    val ancho: Double?,
    val dimensionsNeedingReview: List<String>
)

/** Extracts labelled measurements and unambiguous size pairs from a Spanish transcript. */
object SpokenRequirementParser {
    private val numberAtom = "(?:\\d+(?:[.,]\\d+)?|cero|un|uno|una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez|once|doce|trece|catorce|quince|dieciseis|diecisiete|dieciocho|diecinueve|veinte|treinta|cuarenta|cincuenta)"
    private val numberToken = "($numberAtom(?:\\s+(?:punto|coma)\\s+$numberAtom|\\s+y\\s+(?:uno|un|una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve))?)"
    private val numberWord = mapOf(
        "cero" to 0.0, "un" to 1.0, "uno" to 1.0, "una" to 1.0, "dos" to 2.0, "tres" to 3.0,
        "cuatro" to 4.0, "cinco" to 5.0, "seis" to 6.0, "siete" to 7.0, "ocho" to 8.0, "nueve" to 9.0,
        "diez" to 10.0, "once" to 11.0, "doce" to 12.0, "trece" to 13.0, "catorce" to 14.0,
        "quince" to 15.0, "dieciseis" to 16.0, "diecisiete" to 17.0, "dieciocho" to 18.0,
        "diecinueve" to 19.0, "veinte" to 20.0, "treinta" to 30.0, "cuarenta" to 40.0, "cincuenta" to 50.0
    )

    fun interpret(transcript: String): SpokenRequirement {
        val normalized = normalize(transcript)
        val matches = listOf("carpa" to "Carpas", "toldo" to "Toldos", "tapizado" to "Tapizado de moto")
            .filter { (keyword, _) -> Regex("\\b${Regex.escape(keyword)}s?\\b").containsMatchIn(normalized) }
            .map { it.second }
        val services = matches.distinct()
        val (length, lengthReview) = dimension(normalized, "largo|longitud")
        val (width, widthReview) = dimension(normalized, "ancho")
        val inferredPair = if (length == null && width == null && !lengthReview && !widthReview) {
            dimensionsPair(normalized)
        } else null
        return SpokenRequirement(
            service = services.singleOrNull(),
            ambiguousServices = if (services.size > 1) services else emptyList(),
            largo = length ?: inferredPair?.first,
            ancho = width ?: inferredPair?.second,
            dimensionsNeedingReview = listOfNotNull(
                "largo".takeIf { lengthReview }, "ancho".takeIf { widthReview }
            )
        )
    }

    /** In a service request, an unlabeled “3 x 2” conventionally means length × width. */
    private fun dimensionsPair(text: String): Pair<Double, Double>? {
        val pairPattern = Regex("\\b$numberToken\\s*(?:x|por|por\\s+unos?)\\s*$numberToken\\b")
        val pairs = pairPattern.findAll(text).mapNotNull { match ->
            val first = parseNumber(match.groupValues[1]) ?: return@mapNotNull null
            val second = parseNumber(match.groupValues.last()) ?: return@mapNotNull null
            (first to second).takeIf { first > 0.0 && second > 0.0 }
        }.distinct().toList()
        return pairs.singleOrNull()
    }

    private fun dimension(text: String, label: String): Pair<Double?, Boolean> {
        val beforeLabel = Regex("(?:^|\\b)(?:$label)\\s*(?:es|de|mide|:)?\\s*$numberToken\\b")
        val afterLabel = Regex("\\b$numberToken\\s*(?:metros?|centimetros?|cm|m)?\\s*(?:de\\s*)?(?:$label)\\b")
        val candidates = (beforeLabel.findAll(text) + afterLabel.findAll(text))
            .mapNotNull { it.groupValues.lastOrNull()?.let(::parseNumber) }
            .toList()
        val unique = candidates.distinct()
        return when (unique.size) {
            0 -> null to false
            1 -> unique.single() to false
            else -> null to true
        }
    }

    private fun parseNumber(raw: String): Double? {
        val token = raw.trim()
        token.toDoubleOrNull()?.let { return it }
        val words = token.split(Regex("\\s+"))
        val pointIndex = words.indexOfFirst { it == "punto" || it == "coma" }
        if (pointIndex > 0 && pointIndex < words.lastIndex) {
            val whole = parseIntegerWords(words.subList(0, pointIndex)) ?: return null
            val decimalDigits = words.drop(pointIndex + 1).map { numberWord[it]?.toInt()?.toString() ?: return null }.joinToString("")
            return "$whole.$decimalDigits".toDoubleOrNull()
        }
        return parseIntegerWords(words)?.toDouble()
    }

    private fun parseIntegerWords(words: List<String>): Int? {
        if (words.size == 1) return numberWord[words.first()]?.toInt()
        if (words.size == 3 && words[1] == "y") {
            val tens = numberWord[words[0]]?.toInt() ?: return null
            val units = numberWord[words[2]]?.toInt() ?: return null
            return (tens + units).takeIf { tens >= 20 && units in 1..9 }
        }
        return null
    }

    private fun normalize(value: String): String = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace(',', '.')
}
