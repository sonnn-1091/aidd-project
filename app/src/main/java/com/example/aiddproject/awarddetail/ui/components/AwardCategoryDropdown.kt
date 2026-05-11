package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Category dropdown for the Award Detail screen — Figma nodes
 * `6885:10286` (filter frame) + `6885:10287` (anchor pill).
 *
 * Anchor: 160×40dp pill with `1px #998C5F` outline + 10% SaaCream
 * fill + 4dp radius. Renders the active award's name (14sp Montserrat
 * Normal white, letter-spacing 0.25sp) + 24dp trailing chevron with
 * space-between layout. Clickable region wraps the visual in a
 * `heightIn(min = 48.dp)` Box so the touch target meets TR-008 even
 * though the rendered pill is 40dp tall.
 *
 * Menu: M3 [DropdownMenu] with the same chrome the Language Dropdown
 * established (uUvW6Qm1ve spec § Phase 6 polish) — dark Details-
 * Container-2 surface + gold Details-Border + 8dp radius + 6dp
 * horizontal inset. Each row is a Compose `DropdownMenuItem` at
 * `width(160.dp)` + `heightIn(min = 48.dp)`, M3-default white text
 * color, `selected = (award.id == activeAwardId)` background tint.
 *
 * Idempotent reselection (FR-006 + Language Dropdown FR-006 mirror):
 * tapping the already-active row closes the menu without invoking
 * [onSelect]. Single-click suppression on both anchor open + each
 * row's onClick wraps `rememberSingleClickHandler` per TR-004.
 * Predictive back closes the menu via M3's default
 * `Popup(dismissOnBackPress = true)`.
 *
 * Empty / loading / error states for [categories]: the menu still
 * opens (the anchor is clickable regardless) but the body renders a
 * minimal Text placeholder so the user gets a visual receipt.
 */
@Composable
fun AwardCategoryDropdown(
    categories: AwardsState,
    activeAwardId: String?,
    onSelect: (Award) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val activeName = activeAwardName(categories, activeAwardId)?.let { displayName(it) }
    val a11yLabel = stringResource(R.string.a11y_award_category_dropdown, activeName.orEmpty())
    val expandedState =
        if (expanded) {
            stringResource(R.string.a11y_dropdown_expanded)
        } else {
            stringResource(R.string.a11y_dropdown_collapsed)
        }
    val firstRowFocusRequester = remember { FocusRequester() }
    val anchorClick = rememberSingleClickHandler { expanded = !expanded }

    LaunchedEffect(expanded) {
        if (expanded) {
            runCatching { firstRowFocusRequester.requestFocus() }
        }
    }

    Box(modifier = modifier) {
        Box(
            modifier =
                Modifier
                    .testTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER)
                    .heightIn(min = 48.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = a11yLabel
                        stateDescription = expandedState
                    }.clickableWithoutRipple(anchorClick),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier
                        .width(160.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SaaCream.copy(alpha = 0.10f))
                        .border(
                            width = 1.dp,
                            color = AwardDropdownBorderColor,
                            shape = RoundedCornerShape(4.dp),
                        ).padding(horizontal = 8.dp),
            ) {
                Text(
                    text = activeName.orEmpty(),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.25.sp,
                        ),
                    // Show the full award name; long names like MVP /
                    // Signature 2025 — Creator wrap to a second line.
                    // The earlier single-line+ellipsis fix dropped the
                    // last few characters which made the active award
                    // unreadable — reverted per user feedback.
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(4.dp))
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
            modifier = Modifier.testTag(TEST_TAG_AWARD_DROPDOWN_MENU),
            shape = RoundedCornerShape(8.dp),
            containerColor = AwardMenuSurfaceColor,
            border = BorderStroke(1.dp, AwardDropdownBorderColor),
        ) {
            Column(modifier = Modifier.padding(horizontal = 6.dp)) {
                when (categories) {
                    is AwardsState.Populated -> {
                        categories.items.forEachIndexed { index, award ->
                            val rowClick =
                                rememberSingleClickHandler {
                                    expanded = false
                                    if (award.id != activeAwardId) onSelect(award)
                                }
                            val isSelected = award.id == activeAwardId
                            DropdownMenuItem(
                                onClick = rowClick,
                                colors = MenuDefaults.itemColors(textColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier =
                                    Modifier
                                        .testTag(awardRowTag(award.id))
                                        .width(180.dp)
                                        .heightIn(min = 48.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(
                                                    color = AwardMenuSelectedRowColor,
                                                    shape = RoundedCornerShape(2.dp),
                                                )
                                            } else {
                                                Modifier
                                            },
                                        ).then(
                                            if (index == 0) {
                                                Modifier.focusRequester(firstRowFocusRequester)
                                            } else {
                                                Modifier
                                            },
                                        ),
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                    ) {
                                        Text(
                                            text = displayName(award.name),
                                            color = Color.White,
                                            style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 14.sp,
                                                    lineHeight = 20.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    letterSpacing = 0.25.sp,
                                                ),
                                        )
                                    }
                                },
                            )
                        }
                    }

                    AwardsState.Empty ->
                        EmptyOrErrorRow(text = stringResource(R.string.home_awards_empty))

                    is AwardsState.Error ->
                        EmptyOrErrorRow(text = stringResource(R.string.home_awards_error))

                    AwardsState.Loading ->
                        EmptyOrErrorRow(text = stringResource(R.string.home_awards_loading))
                }
            }
        }
    }
}

@Composable
private fun EmptyOrErrorRow(text: String) {
    Row(
        modifier =
            Modifier
                .width(180.dp)
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        )
    }
}

private fun activeAwardName(
    state: AwardsState,
    activeId: String?,
): String? =
    when (state) {
        is AwardsState.Populated -> state.items.firstOrNull { it.id == activeId }?.name
        else -> null
    }

/**
 * Trim the trailing `" Award"` suffix that `DemoAwardsRepository`'s
 * list rows carry for Home's carousel design (`"Top Talent Award"`).
 * The Award Detail dropdown design — Figma node `6885:10290` — renders
 * just the short name (`"Top Talent"`), so we strip it at the display
 * layer instead of mutating Home's card data. Idempotent: short names
 * pass through unchanged.
 */
private fun displayName(name: String): String = name.removeSuffix(" Award")

private val AwardDropdownBorderColor: Color = Color(0xFF998C5F)
private val AwardMenuSurfaceColor: Color = Color(0xFF00070C)
private val AwardMenuSelectedRowColor: Color = Color(0x33FFEA9E)

const val TEST_TAG_AWARD_DROPDOWN_TRIGGER: String = "award_category_dropdown_trigger"
const val TEST_TAG_AWARD_DROPDOWN_MENU: String = "award_category_dropdown_menu"

fun awardRowTag(awardId: String): String = "award_category_dropdown_row_$awardId"

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
