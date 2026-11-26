package com.leguia.cotizadoria.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CampoContrasenaTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun togglesVisibilityAndKeepsPasswordSelection() {
        val password = mutableStateOf(TextFieldValue("passphrase", selection = TextRange(4)))

        composeRule.setContent {
            CampoContrasena(
                value = password.value,
                onValueChange = { password.value = it },
                label = "Contraseña",
                testTag = "password-field"
            )
        }

        composeRule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        composeRule.onNodeWithContentDescription("Ocultar contraseña").performClick()
        composeRule.runOnIdle {
            assertEquals("passphrase", password.value.text)
            assertEquals(TextRange(4), password.value.selection)
        }

        composeRule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        composeRule.onNodeWithContentDescription("Ocultar contraseña").performClick()
        composeRule.runOnIdle {
            assertEquals("passphrase", password.value.text)
            assertEquals(TextRange(4), password.value.selection)
        }
    }
}
