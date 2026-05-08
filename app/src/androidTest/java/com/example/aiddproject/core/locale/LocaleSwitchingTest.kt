package com.example.aiddproject.core.locale

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * T055 — proves the dual-context override technique in [LanguageProvider] swaps
 * `stringResource()` output across locales without an Activity recreation (SC-004).
 *
 * The test renders the localized `login_description` text inside a [LanguageProvider],
 * starts in VN, flips to EN by mutating the `language` state, and asserts the rendered
 * text changes within the same composition tree.
 */
class LocaleSwitchingTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val vnDescription: String
        get() =
            context
                .createConfigurationContextForLanguage(Language.VN)
                .getString(R.string.login_description)
    private val enDescription: String
        get() =
            context
                .createConfigurationContextForLanguage(Language.EN)
                .getString(R.string.login_description)

    @Test
    fun switching_VN_to_EN_updates_login_description_text_without_activity_recreation() {
        lateinit var setLanguage: (Language) -> Unit
        composeRule.setContent {
            AIDDProjectTheme {
                var lang by remember { mutableStateOf(Language.VN) }
                setLanguage = { lang = it }
                LanguageProvider(language = lang) {
                    Text(text = stringResource(R.string.login_description))
                }
            }
        }

        composeRule.onNodeWithText(vnDescription).assertIsDisplayed()

        composeRule.runOnUiThread { setLanguage(Language.EN) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(enDescription).assertIsDisplayed()
    }
}

/** Helper: build a Context configured for a specific [Language] for assertion-side
 *  resolution of the expected localized string. Mirrors what `LanguageProvider` does
 *  for production rendering. */
private fun android.content.Context.createConfigurationContextForLanguage(language: Language): android.content.Context {
    val overlay =
        android.content.res.Configuration(resources.configuration).apply {
            setLocale(language.toLocale())
        }
    return createConfigurationContext(overlay)
}
