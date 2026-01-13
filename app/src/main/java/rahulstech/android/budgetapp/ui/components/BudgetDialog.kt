package rahulstech.android.budgetapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.ui.screen.BUDGET_PLACEHOLDER

data class BudgetDialogState(
    val showDialog: Boolean = false,
    val isSaving: Boolean = false,
    val budget: Budget = BUDGET_PLACEHOLDER,
)

@Composable
fun BudgetEditDialog(budgetDialogState: BudgetDialogState,
                     onDismiss: ()-> Unit,
                     onClickSave: (Budget)-> Unit,
                 )
{
    val enabled = !budgetDialogState.isSaving
    val budget = budgetDialogState.budget
    var name by rememberSaveable { mutableStateOf(budget.name) }
    var details by rememberSaveable { mutableStateOf(budget.details) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp, alignment = Alignment.End)
            ) {

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = stringResource(R.string.title_edit_budget),
                    style = MaterialTheme.typography.titleLarge
                )

                // cancel
                TextButton(
                    enabled = enabled,
                    onClick = onDismiss
                ) {
                    Text(text = stringResource(R.string.label_cancel))
                }

                // save
                TextButton(
                    enabled = enabled && name.isNotBlank(),
                    onClick = {
                        val newBudget = budget.copy(
                            name = name,
                            details = details
                        )
                        onClickSave(newBudget)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.label_save),
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                value = details,
                onValueChange = { details = it },
                maxLines = 10,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
        }
    }
}

@Composable
fun BudgetDetailsDialog(details: String,
                        onDismiss: () -> Unit)
{
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = stringResource(R.string.label_budget_details),
                    style = MaterialTheme.typography.titleLarge,
                )

                TextButton(
                    onClick = onDismiss,
                ) {
                    Text(text = stringResource(R.string.label_cancel))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = details,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun DeleteBudgetWarningDialog(budget: Budget,
                              onDismiss: ()-> Unit,
                              onClickDelete: (Budget)-> Unit)
{
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.clip(shape = RoundedCornerShape(16.dp)),
        ) {
            Column(
                modifier = Modifier.wrapContentSize().padding(16.dp)
            ) {
                Text(text = stringResource(R.string.title_delete_budget), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(R.string.message_warning_delete_budget))

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    // yes
                    TextButton(onClick = { onClickDelete(budget) }) { Text(stringResource(R.string.label_yes))}

                    // no
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.label_no)) }
                }
            }
        }
    }
}