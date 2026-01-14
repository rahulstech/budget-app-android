package rahulstech.android.budgetapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import rahulstech.android.budgetapp.R

data class DialogAction(
    val label: String,
    val action: ()-> Unit
)

@Composable
fun ConfirmationDialog(onDismiss: ()-> Unit,
                       message: String,
                       actionConfirmLabel: String = stringResource(R.string.label_yes),
                       actionConfirm: ()-> Unit = onDismiss,
                       actionCancelLabel: String = stringResource(R.string.label_no),
                       actionCancel: ()-> Unit = onDismiss,
                       title: String = "")
{
    BasicAlertDialog(onDismissRequest = onDismiss) {
        SimpleAlertDialogMessageContent(title = title, message = message,
            negativeAction = DialogAction(actionConfirmLabel, actionConfirm),
            positiveAction = DialogAction(actionCancelLabel, actionCancel)
        )
    }
}

@Composable
private fun SimpleAlertDialogMessageContent(title: String,
                                            message: String,
                                            positiveAction: DialogAction? = null,
                                            negativeAction: DialogAction? = null,
                                            )
{
    SimpleAlertDialogScaffold(title = title, positiveAction = positiveAction, negativeAction = negativeAction) {
        Text(message)
    }
}

@Composable
private fun SimpleAlertDialogScaffold(title: String,
                                      positiveAction: DialogAction? = null,
                                      negativeAction: DialogAction? = null,
                                      content: @Composable ()-> Unit) {
    Surface(
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).padding(16.dp)
        ) {
            if (title.isNotBlank()) {
                Text(title, style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
            ) {
                content()
            }

            if (null != positiveAction || null != negativeAction) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.End)
                ) {
                    // [Start] (-ve action) (+ve action) [End]

                    if (null != negativeAction) {
                        TextButton(onClick = negativeAction.action) {
                            Text(negativeAction.label)
                        }
                    }

                    if (null != positiveAction) {
                        TextButton(onClick = positiveAction.action) {
                            Text(positiveAction.label)
                        }
                    }
                }
            }
        }
    }

}

@Preview(
    showBackground = true
)
@Composable
fun SimpleAlertDialogMessageContentPreview() {
    SimpleAlertDialogMessageContent(
        title = "Simple Alert",
        message = "This is a simple message of a simple alert",
        positiveAction = DialogAction("Yes"){},
        negativeAction = DialogAction("No"){}
    )
}