package com.leguia.cotizadoria.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminPinHasherTest {
    @Test
    fun `new password policy requires length and character classes`() {
        assertNull(AdminPinHasher.validateNewPassword("CarpaSegura2026!"))
        assertNotNull(AdminPinHasher.validateNewPassword("carpasegura2026!"))
        assertNotNull(AdminPinHasher.validateNewPassword("CarpaSegura2026"))
        assertNotNull(AdminPinHasher.validateNewPassword("Carpa!"))
    }

    @Test
    fun credentialUsesUniqueSaltAndVerifiesOnlyMatchingPin() {
        val first = AdminPinHasher.create("123456")
        val second = AdminPinHasher.create("123456")

        assertNotEquals(first.salt, second.salt)
        assertNotEquals(first.hash, second.hash)
        assertTrue(AdminPinHasher.verify("123456", first.salt, first.hash, first.algorithm))
        assertFalse(AdminPinHasher.verify("654321", first.salt, first.hash, first.algorithm))
    }

    @Test
    fun legacyDigestCanBeVerifiedForOneTimeUpgrade() {
        val legacy = java.security.MessageDigest.getInstance("SHA-256")
            .digest("123456".toByteArray(Charsets.UTF_8))
        val encoded = java.util.Base64.getEncoder().encodeToString(legacy)

        assertTrue(AdminPinHasher.verifyLegacySha256("123456", encoded))
        assertFalse(AdminPinHasher.verifyLegacySha256("654321", encoded))
    }
}
