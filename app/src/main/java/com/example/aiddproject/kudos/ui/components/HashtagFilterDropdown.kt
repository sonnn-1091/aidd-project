package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.domain.Hashtag

/**
 * Hashtag filter — Material 3 [DropdownMenu] matching the chrome of
 * `core/locale/ui/LanguageSelector` + `awarddetail/ui/components/AwardCategoryDropdown`:
 *  - Surface: Details-Container-2 (#00070C)
 *  - Border: 1dp Details-Border (#998C5F), 8dp radius
 *  - 6dp horizontal inset around rows
 *  - Active row tint: SaaCream @ 20% alpha
 *
 * Re-tapping the active hashtag clears the selection ([onSelect] receives null).
 * The dropdown anchors to the trigger via the caller's modifier — the trigger
 * lives in [HighlightFilterRow].
 */
@Composable
fun HashtagFilterDropdown(
    expanded: Boolean,
    hashtags: List<Hashtag>,
    activeHashtagId: String?,
    onSelect: (Hashtag?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        containerColor = MenuSurfaceColor,
        border = BorderStroke(1.dp, MenuBorderColor),
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp)) {
            hashtags.forEach { hashtag ->
                FilterMenuItem(
                    label = "#${hashtag.tagName}",
                    isActive = hashtag.id == activeHashtagId,
                    onTap = {
                        onDismiss()
                        if (hashtag.id == activeHashtagId) onSelect(null) else onSelect(hashtag)
                    },
                )
            }
        }
    }
}

@Composable
internal fun FilterMenuItem(
    label: String,
    isActive: Boolean,
    onTap: () -> Unit,
) {
    val click = rememberSingleClickHandler { onTap() }
    DropdownMenuItem(
        onClick = click,
        colors = MenuDefaults.itemColors(textColor = Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .widthIn(min = 160.dp)
                .heightIn(min = 48.dp)
                .then(
                    if (isActive) {
                        // SaaCream-tinted elevation shadow renders a soft
                        // glow halo around the row; the higher-opacity
                        // background + thin border gives it the brighter
                        // inner fill.
                        Modifier
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(6.dp),
                                ambientColor = MenuGlowColor,
                                spotColor = MenuGlowColor,
                            ).background(
                                color = MenuSelectedRowActiveColor,
                                shape = RoundedCornerShape(6.dp),
                            ).border(
                                width = 1.dp,
                                color = MenuGlowColor,
                                shape = RoundedCornerShape(6.dp),
                            )
                    } else {
                        Modifier
                    },
                ),
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.25.sp,
                        ),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        },
    )
}

internal val MenuSurfaceColor: Color = Color(0xFF00070C)
internal val MenuBorderColor: Color = Color(0xFF998C5F)
internal val MenuSelectedRowColor: Color = Color(0x33FFEA9E)

// Active-row chrome — brighter SaaCream fill + glow shadow for the
// "phát sáng" effect: 60% SaaCream fill so the dark menu surface
// reads through cleanly; the shadow + 1dp SaaCream border carry the
// halo.
private val MenuSelectedRowActiveColor: Color = Color(0x99FFEA9E)
private val MenuGlowColor: Color = Color(0xFFFFEA9E)
