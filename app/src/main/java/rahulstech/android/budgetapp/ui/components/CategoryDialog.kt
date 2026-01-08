package rahulstech.android.budgetapp.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.screen.BUDGET_CATEGORY_PLACEHOLDER
import rahulstech.android.budgetapp.ui.screen.BUDGET_PLACEHOLDER

data class BudgetCategoryDialogState(
    val showDialog: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val category: BudgetCategory = BUDGET_CATEGORY_PLACEHOLDER,
    val budget: Budget = BUDGET_PLACEHOLDER
)

@Composable
fun CategoryDialog(onDismiss: ()-> Unit,
                   onClickSave: (BudgetCategory)-> Unit,
                   categoryDialogState: BudgetCategoryDialogState,
                   )
{
    val enabled = !categoryDialogState.isSaving
    val budget = categoryDialogState.budget
    val initialCategory = categoryDialogState.category
    var name by rememberSaveable { mutableStateOf(initialCategory.name ) }
    var note by rememberSaveable { mutableStateOf(initialCategory.note ) }
    var allocation by rememberSaveable { mutableStateOf(initialCategory.allocation.toString()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet (
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            // top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            ) {
                // title
                Text(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    text = stringResource(R.string.title_new_budget_category),
                    style = MaterialTheme.typography.titleLarge,
                )

                // cancel button
                TextButton(
                    enabled = enabled,
                    onClick = { onDismiss() }
                ) {
                    Text(text = stringResource(R.string.label_cancel))
                }

                // save button
                TextButton(
                    enabled = enabled && name.isNotBlank(),
                    onClick = {
                        val alloc = allocation.toDoubleOrNull() ?: 0.0
                        val category = BudgetCategory(
                            id = initialCategory.id,
                            budgetId = budget.id,
                            name = name,
                            note = note,
                            allocation = alloc
                        )
                        onClickSave(category)
                    }) {
                    Text(text = stringResource(R.string.label_save))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // content
            Column(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(R.string.label_budget_category_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // category note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(text = stringResource(R.string.label_budget_category_note)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                    ),
                    trailingIcon = {
                        if (note.isNotEmpty()) {
                            IconButton(
                                modifier = Modifier.focusable(false),
                                onClick = { note = "" },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.message_clear_text)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // category allocation
                OutlinedTextField(
                    value = allocation,
                    onValueChange = { allocation = it },
                    // TODO: add currency symbol prefix
                    label = { Text(text = stringResource(R.string.label_allocation)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}