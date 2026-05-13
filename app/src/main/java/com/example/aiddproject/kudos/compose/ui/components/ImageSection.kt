package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.UploadedImage
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * F — "Image" label LEFT + thumbnail column RIGHT (Figma `6885:9346`).
 *
 * Row layout (height 72dp, label top-aligned, 12dp gap). The
 * thumbnail column holds:
 *   - A horizontal Row of 32×32dp thumbnails (max 5).
 *   - An "+ Image (Tối đa 5)" pill trigger BELOW the thumbnails.
 *
 * Label "Image" is NOT required — no asterisk, 46dp wide,
 * fontWeight 400.
 */
@Composable
fun ImageSection(
    images: List<UploadedImage>,
    onAddTap: () -> Unit,
    onRemoveImage: (clientId: String) -> Unit,
    @StringRes errorRes: Int?,
    modifier: Modifier = Modifier,
) {
    val atLimit = images.size >= WriteKudoValidators.MAX_IMAGES

    Column(
        modifier = modifier.fillMaxWidth().testTag(WriteKudoTestTags.IMAGE_SECTION),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Label "Image" — no asterisk, fontWeight 400 per Figma.
            Text(
                text = stringResource(R.string.write_kudo_image_label),
                color = FormFieldTokens.LabelColor,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                modifier = Modifier.width(FormFieldTokens.ImageLabelWidth).padding(top = 8.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                if (images.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        images.forEachIndexed { index, image ->
                            ImageThumbnail(image = image, index = index) { onRemoveImage(image.clientId) }
                        }
                    }
                }
                if (!atLimit) {
                    AddImagePill(onClick = onAddTap)
                }
            }
        }
        if (errorRes != null) {
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = FormFieldTokens.RequiredRed,
                modifier = Modifier.padding(start = FormFieldTokens.ImageLabelWidth + 12.dp),
            )
        }
    }
}

@Composable
private fun ImageThumbnail(
    image: UploadedImage,
    index: Int,
    onRemove: () -> Unit,
) {
    val a11yRemove = stringResource(R.string.a11y_write_kudo_remove_image, index + 1)
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag(WriteKudoTestTags.IMAGE_THUMBNAIL_PREFIX + image.clientId),
    ) {
        AsyncImage(
            model = image.localUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)),
        )
        // Small red "×" close button at top-right (matches Figma's
        // small circular badge on each thumbnail).
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove)
                    .testTag(WriteKudoTestTags.IMAGE_REMOVE_BUTTON_PREFIX + image.clientId),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier.size(14.dp).clip(CircleShape).padding(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = a11yRemove,
                    tint = Color.White,
                    modifier =
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape),
                )
            }
        }
    }
}

@Composable
private fun AddImagePill(onClick: () -> Unit) {
    Box(
        modifier =
            Modifier
                .height(32.dp)
                .kudosFieldBox()
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp)
                .testTag(WriteKudoTestTags.IMAGE_ADD_BUTTON),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
                "${stringResource(R.string.write_kudo_image_add)} (${
                    stringResource(R.string.write_kudo_image_limit_note)
                })",
            color = FormFieldTokens.PlaceholderColor,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    }
}
