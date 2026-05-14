package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
                .widthIn(min = 160.dp)
                .heightIn(min = 48.dp)
                .then(
                    if (isActive) {
                        Modifier.background(
                            color = MenuSelectedRowColor,
                            shape = RoundedCornerShape(2.dp),
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
                // Glow on the TEXT only when active — SaaCream color +
                // a same-tinted Shadow with a large blurRadius produces
                // a soft halo around each glyph. Inactive rows stay
                // white at normal weight.
                Text(
                    text = label,
                    color = if (isActive) MenuGlowColor else Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = 0.25.sp,
                            shadow =
                                if (isActive) {
                                    Shadow(
                                        color = MenuGlowColor,
                                        offset = Offset.Zero,
                                        blurRadius = 16f,
                                    )
                                } else {
                                    null
                                },
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

// Active-row text color + same-tinted Shadow tint produces the
// "phát sáng" glow halo around each glyph.
private val MenuGlowColor: Color = Color(0xFFFFEA9E)

// SaaCream @ 20% alpha — selected-row background tint, shared with the
// LanguageSelector / AwardCategory / hashtag picker dropdowns.
private val MenuSelectedRowColor: Color = Color(0x33FFEA9E)
