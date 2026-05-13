package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.LinkDialogState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * C.5 — Link insert dialog (Figma).
 *
 * URL is validated by the VM on Submit; an invalid URL surfaces the
 * inline error inside the dialog (US4 Sc3). The captured selection
 * lives on [LinkDialogState] so the VM's applyLink wraps the correct
 * range even if the textarea has since lost focus.
 */
@Composable
fun LinkInsertDialog(
    state: LinkDialogState,
    onUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.write_kudo_link_dialog_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.url,
                    onValueChange = onUrlChange,
                    label = { Text(stringResource(R.string.write_kudo_link_dialog_url)) },
                    singleLine = true,
                    isError = state.showInvalidError,
                    supportingText =
                        if (state.showInvalidError) {
                            { Text(stringResource(R.string.write_kudo_link_dialog_invalid)) }
                        } else {
                            null
                        },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit) {
                Text(stringResource(R.string.write_kudo_link_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.write_kudo_cancel))
            }
        },
        modifier = Modifier.testTag(WriteKudoTestTags.LINK_DIALOG),
    )
}
