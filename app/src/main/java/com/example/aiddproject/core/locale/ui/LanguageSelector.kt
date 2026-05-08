package com.example.aiddproject.core.locale.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language

/**
 * Language switcher rendered as a flag-pill anchor + Material 3 [DropdownMenu]
 * (US3, T056). Replaces the static `LanguageSelectorView` placeholder shipped during
 * the UI implement pass.
 *
 * - Anchor: 90×32dp pill, 4dp corner radius, padding `4dp 0dp 4dp 8dp`, gap 8dp
 *   (matches MoMorph node `6885:8976`). Renders flag emoji + 2-letter [Language.code]
 *   + Unicode chevron.
 * - Menu: lists exactly the supported set `{VN, EN, JA}` (FR-013) with flag emoji and
 *   localized [Language.nativeName] per spec § Behavioral Accessibility.
 * - Accessibility: anchor `contentDescription` is the localized `a11y_language_selector`
 *   (e.g. "Ngôn ngữ, Tiếng Việt, danh sách thả xuống") so TalkBack reads the *full*
 *   native name even though the visible label is the 2-letter code.
 */
@Composable
fun LanguageSelector(
    selected: Language,
    onSelect: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val a11yLabel = stringResource(R.string.a11y_language_selector, selected.nativeName)

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier =
                Modifier
                    .testTag(TEST_TAG_ANCHOR)
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .padding(start = 8.dp, top = 4.dp, end = 0.dp, bottom = 4.dp)
                    .semantics { contentDescription = a11yLabel }
                    .clickableWithoutRipple { expanded = true },
        ) {
            Text(
                text = selected.flagEmoji,
                fontSize = 18.sp,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = selected.code,
                color = Color.White,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "▾",
                color = Color.White,
                fontSize = 16.sp,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(TEST_TAG_MENU),
        ) {
            Language.entries.forEach { lang ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        if (lang != selected) onSelect(lang)
                    },
                    modifier = Modifier.testTag(menuItemTag(lang)),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = lang.flagEmoji, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = lang.nativeName,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                )
            }
        }
    }
}

const val TEST_TAG_ANCHOR: String = "language_selector_anchor"
const val TEST_TAG_MENU: String = "language_selector_menu"

fun menuItemTag(language: Language): String = "language_selector_item_${language.code}"

/** Minimal click modifier without ripple — visual matches the Figma anchor. */
@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
    )
}
