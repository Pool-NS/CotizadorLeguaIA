package com.leguia.cotizadoria.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Password KDF for the device-local demonstration gate. This is not server authentication. */
object AdminPinHasher {
    private const val ITERATIONS = 210_000
    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256
    private const val PBKDF2_SHA256 = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_SHA1 = "PBKDF2WithHmacSHA1"

    data class Credential(val salt: String, val hash: String, val algorithm: String)

    fun validateNewPassword(password: String): String? = when {
        password.length < 12 -> "Usa al menos 12 caracteres."
        password.length > 64 -> "Usa como máximo 64 caracteres."
        password.none(Char::isUpperCase) -> "Agrega al menos una letra mayúscula."
        password.none(Char::isLowerCase) -> "Agrega al menos una letra minúscula."
        password.none(Char::isDigit) -> "Agrega al menos un número."
        password.none { !it.isLetterOrDigit() && !it.isWhitespace() } ->
            "Agrega al menos un símbolo (por ejemplo: !, @ o #)."
        else -> null
    }

    fun create(pin: String): Credential {
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val algorithm = supportedAlgorithm()
        return Credential(encode(salt), encode(derive(pin, salt, algorithm)), algorithm)
    }

    fun verify(pin: String, saltHex: String, expectedHashHex: String, algorithm: String): Boolean = runCatching {
        val salt = decodeHex(saltHex)
        val expected = decodeHex(expectedHashHex)
        MessageDigest.isEqual(expected, derive(pin, salt, algorithm))
    }.getOrDefault(false)

    fun verifyLegacySha256(pin: String, expectedHashBase64: String): Boolean = runCatching {
        val actual = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        MessageDigest.isEqual(expectedHashBase64.toByteArray(Charsets.US_ASCII), encodeBase64(actual).toByteArray(Charsets.US_ASCII))
    }.getOrDefault(false)

    private fun supportedAlgorithm(): String = try {
        SecretKeyFactory.getInstance(PBKDF2_SHA256)
        PBKDF2_SHA256
    } catch (_: Exception) {
        // PBKDF2WithHmacSHA256 is available from Android API 26; keep API 24-25 usable.
        PBKDF2_SHA1
    }

    private fun derive(pin: String, salt: ByteArray, algorithm: String): ByteArray {
        require(algorithm == PBKDF2_SHA256 || algorithm == PBKDF2_SHA1) { "Unsupported PIN KDF." }
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(value: ByteArray): String = buildString(value.size * 2) {
        value.forEach { byte ->
            val unsigned = byte.toInt() and 0xff
            append(HEX[unsigned ushr 4])
            append(HEX[unsigned and 0x0f])
        }
    }

    private fun decodeHex(value: String): ByteArray {
        require(value.length % 2 == 0) { "Invalid PIN credential." }
        return ByteArray(value.length / 2) { index ->
            val high = value[index * 2].digitToInt(16)
            val low = value[index * 2 + 1].digitToInt(16)
            ((high shl 4) or low).toByte()
        }
    }

    private fun encodeBase64(value: ByteArray): String = buildString(((value.size + 2) / 3) * 4) {
        var index = 0
        while (index < value.size) {
            val first = value[index].toInt() and 0xff
            val hasSecond = index + 1 < value.size
            val hasThird = index + 2 < value.size
            val second = if (hasSecond) value[index + 1].toInt() and 0xff else 0
            val third = if (hasThird) value[index + 2].toInt() and 0xff else 0
            append(BASE64[first ushr 2])
            append(BASE64[((first and 0x03) shl 4) or (second ushr 4)])
            append(if (hasSecond) BASE64[((second and 0x0f) shl 2) or (third ushr 6)] else '=')
            append(if (hasThird) BASE64[third and 0x3f] else '=')
            index += 3
        }
    }

    private const val HEX = "0123456789abcdef"
    private const val BASE64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
}
