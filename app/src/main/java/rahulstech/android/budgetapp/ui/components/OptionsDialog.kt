package rahulstech.android.budgetapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class OptionItem(
    val label: String,
    // don't call onDismiss inside onClick
    val onClick: ()-> Unit,
    val enabled: Boolean = true,
)

@Composable
fun OptionsDialog(onDismiss: ()-> Unit,
                  title: String = "",
                  vararg options: OptionItem)
{
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetMaxWidth = 360.dp
    ) {
        OptionsDialogContent(title,onDismiss, *options)
    }
}

@Composable
private fun OptionsDialogContent(title: String = "",
                                 dismissCallback: ()-> Unit = {},
                                 vararg options: OptionItem)
{
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        if (title.isNotBlank()) {
            Text(title, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
        ) {
            itemsIndexed(items = options) { index, option ->
                if (index > 0) {
                    HorizontalDivider()
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                        .clickable(
                            enabled = option.enabled,
                            onClick = {
                                dismissCallback()
                                option.onClick()
                            }
                        ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(option.label,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
fun OptionsDialogContentPreview() {
    OptionsDialogContent(
        title = "Options",
        options = arrayOf(
            OptionItem("Edit",{}),
            OptionItem("Delete",{})
        )
    )
}