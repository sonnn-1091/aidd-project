package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Form-field design tokens shared by the Viết Kudo composer
 * sub-composables (queried 2026-05-13 from Figma `7fFAb-K35a`).
 *
 *   - Field label colour: `#00101A` (Montserrat Medium 14sp)
 *   - Required-marker red: `#CF1322`
 *   - Input border: 0.5dp `#998C5F`
 *   - Input radius: 3.5dp
 *   - Input fill: `#FFFFFF`
 *   - Input padding: 7dp vertical, 11dp horizontal (rounded from
 *     7.149 / 10.723 in Figma)
 *   - Input height: 40dp
 *   - Placeholder text: Montserrat 400 12sp `#999999`
 *
 * Section row gap: 16dp.
 */
object FormFieldTokens {
    val LabelColor: Color = Color(0xFF00101A)
    val RequiredRed: Color = Color(0xFFCF1322)
    val PlaceholderColor: Color = Color(0xFF999999)
    val BorderGold: Color = Color(0xFF998C5F)
    val FieldFill: Color = Color(0xFFFFFFFF)
    val FormCardBackground: Color = Color(0xFFFFF8E1)
    val FieldRadius = 3.5.dp
    val FieldHeight = 40.dp
    val FieldHorizontalPadding = 11.dp
    val FieldVerticalPadding = 7.dp
    val LabelWidth = 93.dp // recipient + award label cell — wide enough for "Người nhận *"
    val HashtagLabelWidth = 70.dp
    val ImageLabelWidth = 46.dp
}

/**
 * Inline form-field label — renders the localized text + a red `*`
 * required marker in a fixed-width Row, vertically centered. Used by
 * recipient / title / hashtag / image rows so labels sit BESIDE
 * their input instead of above.
 */
@Composable
fun KudosFieldLabel(
    text: String,
    required: Boolean,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = FormFieldTokens.LabelWidth,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier.width(width),
    ) {
        Text(
            text = text,
            color = FormFieldTokens.LabelColor,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Medium,
        )
        if (required) {
            Spacer(Modifier.width(1.dp))
            Text(
                text = "*",
                color = FormFieldTokens.RequiredRed,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Shared Modifier chain for the white pill-shaped input box (40dp tall,
 * 0.5dp gold border, 3.5dp radius). Used by the recipient dropdown
 * trigger + title text input + hashtag/image add buttons.
 */
fun Modifier.kudosFieldBox(): Modifier =
    this
        .height(FormFieldTokens.FieldHeight)
        .clip(RoundedCornerShape(FormFieldTokens.FieldRadius))
        .background(FormFieldTokens.FieldFill)
        .border(0.5.dp, FormFieldTokens.BorderGold, RoundedCornerShape(FormFieldTokens.FieldRadius))
