package com.example.aiddproject.core.locale.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.ui.rememberSingleClickHandler

/**
 * Language switcher rendered as a flag-pill anchor + Material 3
 * [DropdownMenu]. Shared between Login and Home (and any future chrome
 * screen) — see Language Dropdown spec `uUvW6Qm1ve`.
 *
 * - **Anchor**: visual pill is 32dp tall × ~90dp wide (Figma `6885:8976`).
 *   The click region wraps the visual in a 48dp-min `Box` so the touch
 *   target meets Constitution Principle III without changing the rendered
 *   chrome (TR-008).
 * - **Menu**: lists `Language.entries` — currently `[VN, EN]`. JA was
 *   removed per spec § Resolved Q1; orphaned-`"JA"` DataStore tokens
 *   silently fall back to VN via `Language.fromCode`.
 * - **Accessibility (TR-003)**: anchor surfaces `Role.Button` with a
 *   localized `contentDescription` (`a11y_language_selector` formatted
 *   with the active language's `nativeName`) AND a localized
 *   `stateDescription` that flips between `a11y_dropdown_collapsed` and
 *   `a11y_dropdown_expanded`. When the menu opens, TalkBack focus moves
 *   to the **first row** via a `FocusRequester` triggered by a
 *   `LaunchedEffect(expanded)`.
 * - **Double-tap suppression (TR-004)**: anchor open + each row's
 *   `onClick` are wrapped in `rememberSingleClickHandler` so a
 *   finger-bounce double-tap can never push two locale changes (or two
 *   menu opens) through the system.
 * - **Idempotent reselection (TR-005 UI-side)**: the existing
 *   `if (lang != selected) onSelect(lang)` short-circuit is preserved
 *   inside the wrapped lambda — re-tapping the active language closes
 *   the menu without invoking `onSelect`.
 */
@Composable
fun LanguageSelector(
    selected: Language,
    onSelect: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val a11yLabel = stringResource(R.string.a11y_language_selector, selected.nativeName)
    val expandedState =
        if (expanded) {
            stringResource(R.string.a11y_dropdown_expanded)
        } else {
            stringResource(R.string.a11y_dropdown_collapsed)
        }
    val firstRowFocusRequester = remember { FocusRequester() }
    val anchorClick = rememberSingleClickHandler { expanded = !expanded }

    // Move TalkBack / keyboard focus to the first row whenever the menu
    // opens. The effect intentionally does NOT run on close so focus
    // returns to the anchor naturally per M3 default.
    LaunchedEffect(expanded) {
        if (expanded) {
            // The first DropdownMenuItem is composed asynchronously after
            // `expanded` flips true; M3 schedules the request through the
            // composition's focus owner so the call is safe even before
            // the row attaches its `Modifier.focusRequester`.
            runCatching { firstRowFocusRequester.requestFocus() }
        }
    }

    Box(modifier = modifier) {
        // Outer 48dp-min Box absorbs the click so the touch target meets
        // TR-008 while the inner Row keeps the 32dp visual pill (Figma).
        Box(
            modifier =
                Modifier
                    .testTag(TEST_TAG_ANCHOR)
                    .heightIn(min = 48.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = a11yLabel
                        stateDescription = expandedState
                    }.clickableWithoutRipple(anchorClick),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier =
                    Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .padding(start = 8.dp, top = 4.dp, end = 0.dp, bottom = 4.dp),
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
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag(TEST_TAG_MENU),
        ) {
            Language.entries.forEachIndexed { index, lang ->
                val rowClick =
                    rememberSingleClickHandler {
                        expanded = false
                        if (lang != selected) onSelect(lang)
                    }
                DropdownMenuItem(
                    onClick = rowClick,
                    modifier =
                        Modifier
                            .testTag(menuItemTag(lang))
                            .heightIn(min = 48.dp)
                            .then(
                                if (index == 0) {
                                    Modifier.focusRequester(firstRowFocusRequester)
                                } else {
                                    Modifier
                                },
                            ),
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
