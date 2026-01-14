package rahulstech.android.budgetapp.ui.components

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.screen.BUDGET_CATEGORY_PLACEHOLDER
import rahulstech.android.budgetapp.ui.screen.EXPENSE_PLACEHOLDER
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

data class ExpenseDialogState(
    val showDialog: Boolean = false,
    val isSaving: Boolean = false,
    val budgetId: Long = 0,
    val category: BudgetCategory = BUDGET_CATEGORY_PLACEHOLDER,
    val canChooseCategory: Boolean = true,
    val expense: Expense = EXPENSE_PLACEHOLDER,
)

private val EXPENSE_DATE_FORMATER = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")

private val LocalDateSaver = Saver<LocalDate, Long>(
    save = { it.toEpochDay() },
    restore = { LocalDate.ofEpochDay(it) }
)

@Composable
fun ExpenseDialog(expenseDialogState: ExpenseDialogState,
                  onDismiss: ()-> Unit,
                  onSaveExpense: (Expense)-> Unit,
                  )
{
    val enabled = !expenseDialogState.isSaving
    val initialExpense = expenseDialogState.expense
    var amount by rememberSaveable { mutableStateOf(initialExpense.amount.toString()) }
    var note by rememberSaveable { mutableStateOf(initialExpense.note) }
    var date by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf(initialExpense.date) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showCategoryChooser by rememberSaveable { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(expenseDialogState.category) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    text = stringResource(R.string.title_add_expense),
                    style = MaterialTheme.typography.titleLarge,
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
                    enabled = enabled && amount.isNotEmpty() && BUDGET_CATEGORY_PLACEHOLDER != selectedCategory,
                    onClick = {
                        val expense = Expense(
                            id = initialExpense.id,
                            budgetId = selectedCategory.budgetId,
                            categoryId = selectedCategory.id,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            note = note,
                            date = date
                        )
                        onSaveExpense(expense)
                    }
                ) {
                    Text(text = stringResource(R.string.label_save))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                // category
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = selectedCategory.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_budget_category)) },
                    singleLine = true,
                    trailingIcon = {
                        if (expenseDialogState.canChooseCategory) {
                            IconButton(onClick = { showCategoryChooser = true }) {
                                Icon(Icons.Default.ArrowDropDown, stringResource(R.string.message_expense_date_picker))
                            }
                        }
                    }
                )

                // date
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = EXPENSE_DATE_FORMATER.format(date),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = stringResource(R.string.label_expense_amount)) },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Rounded.DateRange,
                                contentDescription = stringResource(R.string.message_expense_date_picker)
                            )
                        }
                    }
                )

                // amount
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(text = stringResource(R.string.label_expense_amount)) },
                    // TODO: add currency symbol prefix
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                )

                // note
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(text = stringResource(R.string.label_expense_note)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    singleLine = true,
                    trailingIcon = {
                        if (note.isNotEmpty()) {
                            IconButton(onClick = { note = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.message_clear_text)
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        ExpenseDatePicker(
            initialDate = date,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                showDatePicker = false
                date = it
            }
        )
    }

    if (showCategoryChooser) {
        CategoryChooserDialog(
            budgetId = expenseDialogState.budgetId,
            onDismiss = { showCategoryChooser = false },
            onClickChoose = { categories ->
                showCategoryChooser = false
                selectedCategory = if (categories.isEmpty()) BUDGET_CATEGORY_PLACEHOLDER else categories.first()
            }
        )
    }
}

@Composable
fun ExpenseDatePicker(initialDate: LocalDate = LocalDate.now(),
                      onDismiss: () -> Unit,
                      onDateSelected: (LocalDate)-> Unit,
                      )
{
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = LocalDate.ofEpochDay(TimeUnit.MILLISECONDS.toDays(millis))
                    onDateSelected(date)
                }
            }) {
                Text(text = stringResource(R.string.label_choose))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.label_cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
