package com.leguia.cotizadoria.ai

import java.io.IOException
import java.net.SocketTimeoutException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiErrorMessageTest {
    @Test
    fun connectionMessageDoesNotExposeInternalAddressOrTrace() {
        val message = IOException("failed to connect to /192.168.100.3 port 8000").toSafeUserMessage()

        assertTrue(message.contains("No pudimos conectar"))
        assertFalse(message.contains("192.168.100.3"))
        assertFalse(message.contains("8000"))
    }

    @Test
    fun timeoutOffersManualAlternative() {
        val message = SocketTimeoutException("read timed out").toSafeUserMessage()

        assertTrue(message.contains("Reintenta"))
        assertTrue(message.contains("manualmente"))
        assertFalse(message.contains("read timed out"))
    }
}
