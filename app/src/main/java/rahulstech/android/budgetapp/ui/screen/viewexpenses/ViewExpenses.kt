package rahulstech.android.budgetapp.ui.screen.viewexpenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.ExpenseFilterParams
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.ConfirmationDialog
import rahulstech.android.budgetapp.ui.components.ExpenseDialog
import rahulstech.android.budgetapp.ui.components.OptionItem
import rahulstech.android.budgetapp.ui.components.OptionsDialog
import rahulstech.android.budgetapp.ui.components.shimmer
import rahulstech.android.budgetapp.ui.screen.FAB
import rahulstech.android.budgetapp.ui.screen.IconValue
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.ScaffoldState
import rahulstech.android.budgetapp.ui.screen.ScaffoldStateCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIText
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "ViewExpenses"

private val EXPENSE_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd-MMMM-yyyy")

@Composable
fun ViewExpensesRoute(budgetId: Long,
                      categoryId: Long? = null,
                      scaffoldStateCallback: ScaffoldStateCallback,
                      snackBarCallback: SnackBarCallback,
                      navigationCallback: NavigationCallback,
                      viewModel: ViewExpensesViewModel = hiltViewModel())
{
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.filterExpenses(
            ExpenseFilterParams(
                budgetId = budgetId,
                categories = categoryId?.let { listOf(it) } ?: emptyList()
            )
        )

        viewModel.effect.collect { effect ->
            when(effect) {
                is UISideEffect.ShowSnackBar -> {
                    val message = when(effect.message) {
                        is UIText.StringResource -> effect.message.resolveString(context)
                        is UIText.PlainString -> effect.message.value
                    }
                    snackBarCallback(SnackBarEvent(message = message))
                }
                is UISideEffect.NavigateTo -> {
                    navigationCallback(effect.event)
                }
                is UISideEffect.ExitScreen -> {
                    navigationCallback(NavigationEvent.Exit())
                }
            }
        }
    }

    scaffoldStateCallback(ScaffoldState(
        showNavUp = true,
        title = stringResource(R.string.title_view_expenses),
        floatingActionButton = FAB.IconAndText(
            icon = IconValue.VectorIcon(Icons.Default.Add),
            label = stringResource(R.string.label_add_expense),
            onClick = { viewModel.showAddExpenseDialog(budgetId) }
        )
    ))

    val expenses = viewModel.expenses.collectAsLazyPagingItems()
    ViewExpensesScreen(
        expenses = expenses,
        onUIEvent = viewModel::onUIEvent,
    )

    val uiState by viewModel.state.collectAsStateWithLifecycle()

    if (uiState.addExpenseDialog.showDialog) {
        ExpenseDialog(
            expenseDialogState = uiState.addExpenseDialog,
            onDismiss = { viewModel.hideAddExpenseDialog() },
            onSaveExpense = { viewModel.addExpense(it) }
        )
    }

    if (uiState.expenseOptionsDialog.showDialog) {
        ExpenseOptionDialog(
            expense = uiState.expenseOptionsDialog.expense,
            onDismiss = { viewModel.hideExpenseOptionsDialog() },
            onUIEvent = {
                viewModel.hideExpenseOptionsDialog()
                viewModel.onUIEvent(it)
            }
        )
    }

    if (uiState.editExpenseDialog.showDialog) {
        ExpenseDialog(
            expenseDialogState = uiState.editExpenseDialog,
            onDismiss = { viewModel.hideEditExpenseDialog() },
            onSaveExpense = { viewModel.editExpense(it) }
        )
    }

    if (uiState.deleteExpenseDialog.showDialog) {
        DeleteExpenseWarningDialog(
            expense = uiState.deleteExpenseDialog.expense,
            onDismiss = { viewModel.hideDeleteExpenseDialog() },
            onClickDelete = {
                viewModel.hideDeleteExpenseDialog()
                viewModel.removeExpense(it)
            }
        )
    }
}

@Composable
fun ViewExpensesScreen(expenses: LazyPagingItems<ExpenseListItem>,
                       onUIEvent: ViewExpenseUIEventCallback = {}
                       )
{
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {

        when(expenses.loadState.prepend) {
            is LoadState.Loading -> {
                item {
                    ExpenseItemPlaceholder()
                }
            }
            else -> {}
        }

        when(expenses.loadState.refresh) {
            is LoadState.Loading -> {
                items(count = 10) {
                    ExpenseItemPlaceholder()
                }
            }
            is LoadState.Error -> {}
            is LoadState.NotLoading -> {
                if (expenses.itemCount == 0) {
                    item {
                        EmptyView()
                    }
                }
                else {
                    items(count = expenses.itemCount, key = { index -> expenses[index]!!.key }) { index ->
                        when(val item = expenses[index]) {
                            is ExpenseListItem.ItemHeader -> {
                                ExpenseHeader(item.date)
                            }
                            is ExpenseListItem.ItemExpense -> {
                                ExpenseItem(
                                    item.value,
                                    onClickOptions = {
                                        onUIEvent(ViewExpenseUIEvent.ShowExpenseOptionsDialogEvent(item.value))
                                    }
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        when(expenses.loadState.append) {
            is LoadState.Loading -> {
                item {
                    ExpenseItemPlaceholder()
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ExpenseHeader(date: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp)
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.DateRange, contentDescription = null)

        Text(date.format(EXPENSE_DATE_FORMAT))
    }
}

@Composable
fun ExpenseItem(expense: Expense, onClickOptions: (Expense)-> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 8.dp, top = 12.dp),
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = expense.amount.toString(),
                    style = MaterialTheme.typography.titleMedium,
                )

                if (expense.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = expense.note,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            FilledTonalIconButton (onClick = { onClickOptions(expense) }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.message_show_more_options))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.clip(shape = MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            text = expense.category!!.name,
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider()
    }
}

@Composable
fun ExpenseItemPlaceholder() {
    Row (modifier = Modifier.fillMaxWidth().height(88.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(88.dp).shimmer())

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(R.string.label_no_expense), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ExpenseOptionDialog(expense: Expense,
                        onDismiss: ()-> Unit,
                        onUIEvent: ViewExpenseUIEventCallback,
                        )
{
    OptionsDialog(
        onDismiss = onDismiss,
        options = arrayOf(
            OptionItem(stringResource(R.string.label_edit), { onUIEvent(ViewExpenseUIEvent.ShowEditExpenseDialogEvent(expense)) }),
            OptionItem(stringResource(R.string.label_delete), { onUIEvent(ViewExpenseUIEvent.ShowDeleteExpenseDialogEvent(expense))})
        )
    )
}

@Composable
fun DeleteExpenseWarningDialog(expense: Expense,
                               onDismiss: ()-> Unit,
                               onClickDelete: (Expense)-> Unit)
{
    ConfirmationDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.title_delete_expense),
        message = stringResource(R.string.message_warning_delete_expense),
        actionConfirmLabel = stringResource(R.string.label_yes),
        actionConfirm = { onClickDelete(expense) },
        actionCancelLabel = stringResource(R.string.label_no)
    )
}


@Preview(showBackground = true)
@Composable
fun ExpenseItemPreview() {
    ExpenseItem(
        Expense(
            id = 1,
            budgetId = 1,
            categoryId = 1,
            amount = 1250.26,
            note = "This is the expense note",
            category = BudgetCategory(
                id = 1,
                budgetId = 1,
                name = "Fuel"
            )
        ),
        onClickOptions = {}
    )
}