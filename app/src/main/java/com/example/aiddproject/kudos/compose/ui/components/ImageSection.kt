package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.UploadedImage
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * F — Image attachment section (Figma `6885:9346`).
 *
 * Q-W-2: thumbnails render from the local Uri via Coil — no Storage
 * upload happens at pick time. The submit flow uploads + INSERTs in
 * a single coroutine; per-image failures roll back via
 * KudosRepository.deleteKudoImage.
 *
 * The "+ Image" button hides when [images] has 5 entries per
 * spec § US5 Sc2.
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
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag(WriteKudoTestTags.IMAGE_SECTION),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.write_kudo_image_label),
            style = MaterialTheme.typography.labelMedium,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            images.forEachIndexed { index, image ->
                ImageThumbnail(
                    image = image,
                    index = index,
                    onRemove = { onRemoveImage(image.clientId) },
                )
            }
            if (!atLimit) {
                OutlinedButton(
                    onClick = onAddTap,
                    modifier =
                        Modifier
                            .size(width = 88.dp, height = 88.dp)
                            .testTag(WriteKudoTestTags.IMAGE_ADD_BUTTON),
                ) {
                    Text(text = stringResource(R.string.write_kudo_image_add))
                }
            }
        }
        Text(
            text =
                if (errorRes != null) {
                    stringResource(errorRes)
                } else {
                    stringResource(R.string.write_kudo_image_limit_note)
                },
            style = MaterialTheme.typography.bodySmall,
            color =
                if (errorRes != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
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
                .size(88.dp)
                .clip(RoundedCornerShape(8.dp))
                .testTag(WriteKudoTestTags.IMAGE_THUMBNAIL_PREFIX + image.clientId),
    ) {
        AsyncImage(
            model = image.localUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(8.dp)),
        )
        IconButton(
            onClick = onRemove,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .testTag(WriteKudoTestTags.IMAGE_REMOVE_BUTTON_PREFIX + image.clientId),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = a11yRemove,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
