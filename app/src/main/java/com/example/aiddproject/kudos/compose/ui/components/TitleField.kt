package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * B.3 + B.4 — "Danh hiệu *" label LEFT + text input RIGHT (Figma
 * `6885:9298` / `6885:9302`).
 *
 * Custom-styled input (BasicTextField inside a `kudosFieldBox()`)
 * instead of M3 OutlinedTextField so the height stays at the Figma-
 * spec 40dp and the border is the thin 0.5dp gold per design.
 */
@Composable
fun TitleField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes errorRes: Int?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().height(FormFieldTokens.FieldHeight),
        ) {
            KudosFieldLabel(
                text = stringResource(R.string.write_kudo_title_label),
                required = true,
            )
            Box(
                modifier =
                    Modifier
                        .width(FormFieldTokens.InputColumnWidth)
                        .kudosFieldBox()
                        .padding(
                            horizontal = FormFieldTokens.FieldHorizontalPadding,
                            vertical = FormFieldTokens.FieldVerticalPadding,
                        )
                        .testTag(WriteKudoTestTags.TITLE_INPUT),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.write_kudo_title_placeholder),
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
                modifier = Modifier.padding(start = FormFieldTokens.LabelWidth + 8.dp),
            )
        }
    }
}
