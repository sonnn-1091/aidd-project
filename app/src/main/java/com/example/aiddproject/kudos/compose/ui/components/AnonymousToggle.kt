package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * G — "Gửi lời cám ơn và ghi nhận ẩn danh" checkbox (Figma `6885:9363`).
 *
 * Custom checkbox visual: 20dp square, 1dp gold border. When checked,
 * a `#998C5F`-filled inner square paints inside a 4dp inset (no check
 * icon per design). Toggling on reveals the "Nickname ẩn danh *" row —
 * a required text field for the visible-nickname when the sender is
 * hidden.
 */
@Composable
fun AnonymousToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    @StringRes nicknameErrorRes: Int?,
    modifier: Modifier = Modifier,
) {
    val stateDesc =
        stringResource(
            if (checked) R.string.a11y_write_kudo_anonymous_checked else R.string.a11y_write_kudo_anonymous_unchecked,
        )
    val label = stringResource(R.string.write_kudo_anonymous_label)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Checkbox) { onCheckedChange(!checked) }
                    .padding(horizontal = 8.dp)
                    .testTag(WriteKudoTestTags.ANONYMOUS_TOGGLE)
                    .semantics {
                        role = Role.Checkbox
                        contentDescription = label
                        stateDescription = stateDesc
                    },
        ) {
            AnonymousCheckBox(checked = checked)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = FormFieldTokens.LabelColor,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        if (checked) {
            AnonymousNicknameField(
                value = nickname,
                onValueChange = onNicknameChange,
                errorRes = nicknameErrorRes,
            )
        }
    }
}

/**
 * Filled-square checkbox visual — 20dp outer, 1dp gold border, 2dp
 * radius. Checked state renders a `BorderGold`-filled inner square
 * inset by 4dp from the border. No check glyph per design request.
 */
@Composable
private fun AnonymousCheckBox(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, FormFieldTokens.BorderGold, RoundedCornerShape(2.dp))
                .padding(4.dp),
    ) {
        if (checked) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(FormFieldTokens.BorderGold),
            )
        }
    }
}

/**
 * "Nickname ẩn danh *" row — label LEFT, white pill input RIGHT,
 * matching the [TitleField] visual chrome. Surfaces the inline error
 * below when the form attempts submit with a blank nickname.
 */
@Composable
private fun AnonymousNicknameField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes errorRes: Int?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().height(FormFieldTokens.FieldHeight),
        ) {
            KudosFieldLabel(
                text = stringResource(R.string.write_kudo_anonymous_nickname_label),
                required = true,
                width = AnonymousNicknameLabelWidth,
            )
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .kudosFieldBox()
                        .padding(
                            horizontal = FormFieldTokens.FieldHorizontalPadding,
                            vertical = FormFieldTokens.FieldVerticalPadding,
                        )
                        .testTag(WriteKudoTestTags.ANONYMOUS_NICKNAME_INPUT),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.write_kudo_anonymous_nickname_placeholder),
                            color = FormFieldTokens.PlaceholderColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle =
                            LocalTextStyle.current.copy(
                                color = FormFieldTokens.LabelColor,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                            ),
                        singleLine = true,
                        cursorBrush = SolidColor(FormFieldTokens.LabelColor),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        if (errorRes != null) {
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = FormFieldTokens.RequiredRed,
                modifier = Modifier.padding(start = AnonymousNicknameLabelWidth + 8.dp),
            )
        }
    }
}

private val AnonymousNicknameLabelWidth = 132.dp
