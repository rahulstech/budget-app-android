package rahulstech.android.budgetapp.ui.screen.viewexpenses

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import rahulstech.android.budgetapp.ui.components.ExpenseDialog
import rahulstech.android.budgetapp.ui.components.shimmer
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.UIState
import rahulstech.android.budgetapp.ui.theme.primaryTopAppBarColors
import java.time.LocalDate

private const val TAG = "ViewExpenses"

@Composable
fun ViewExpensesRoute(budgetId: Long,
                      categoryId: Long? = null,
                      snackBarCallback: SnackBarCallback,
                      navigateTo: NavigationCallback,
                      viewModel: ViewExpensesViewModel = hiltViewModel())
{
    val expenseDialogState by viewModel.expenseDialogState.collectAsStateWithLifecycle()

    LaunchedEffect(budgetId, categoryId) {
        viewModel.filterExpenses(
            ExpenseFilterParams(
                budgetId = budgetId,
                categories = categoryId?.let { listOf(it) } ?: emptyList()
            )
        )
    }

    val expenses = viewModel.expenses.collectAsLazyPagingItems()
    ViewExpensesScreen(
        expenses = expenses,
        navigateTo = navigateTo,
        onClickAddExpense = {
            viewModel.expenseDialogStateManager.showDialog(budgetId)
        }
    )

    if (expenseDialogState.showDialog) {
        ExpenseDialog(
            expenseDialogState = expenseDialogState,
            onDismiss = { viewModel.expenseDialogStateManager.hideDialog() },
            onSaveExpense = {
                // ExpenseDialog already set the budgetId and categoryId to the expense
                viewModel.expenseDialogStateManager.updateSaving(true, it)
                viewModel.addExpense(it)
            }
        )
    }

    val addExpenseState by viewModel.addExpenseState.collectAsStateWithLifecycle(UIState.Idle())
    when(addExpenseState) {
        is UIState.Loading -> {
            viewModel.expenseDialogStateManager.updateSaving(true)
        }
        is UIState.Error -> {
            Log.e(TAG, "add expense error", (addExpenseState as UIState.Error).cause)
            snackBarCallback(SnackBarEvent(message = stringResource(R.string.message_expense_add_error)))
            viewModel.expenseDialogStateManager.updateSaving(false)
        }
        is UIState.Success -> {
            viewModel.expenseDialogStateManager.hideDialog()
            snackBarCallback(SnackBarEvent(message = stringResource(R.string.message_expense_add_successful)))
        }
        else -> {}
    }
}

@Composable
fun ViewExpensesScreen(expenses: LazyPagingItems<ExpenseListItem>,
                       navigateTo: NavigationCallback = {},
                       onClickAddExpense: ()-> Unit = {},
                       )
{
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        topBar = {
            TopAppBar(
                colors = primaryTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(R.string.title_view_expenses),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    // filter
                    IconButton(onClick = { /* TODO: implement filter expenses click */ }) {
                        Icon(painterResource(R.drawable.baseline_filter_list_alt_24),
                            stringResource(R.string.message_filter_expenses))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigateTo(NavigationEvent.Exit()) }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.message_navigate_up)
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(percent = 50),
                onClick = onClickAddExpense
            ) {
                Icon(Icons.Default.Add, null)

                Spacer(modifier = Modifier.width(8.dp))

                Text(stringResource(R.string.label_add_expense))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {

                when(expenses.loadState.prepend) {
                    is LoadState.Loading -> {
                        item {
                            PlaceholderItem()
                        }
                    }
                    else -> {}
                }

                when(expenses.loadState.refresh) {
                    is LoadState.Loading -> {
                        items(count = 10) {
                            PlaceholderItem()
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
                                        ExpenseItem(item.value)
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
                            PlaceholderItem()
                        }
                    }
                    else -> {}
                }
            }
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

        Text(text = date.toString()) // TODO: formate expense header date
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
    ) {
        Text(
            text = expense.amount.toString(),
            style = MaterialTheme.typography.titleLarge,
        )

        if (expense.note.isNotBlank()) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = expense.note,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SuggestionChip(
            onClick = {},
            label = {
                Text(text = expense.category!!.name)
            },
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider()
    }
}

@Composable
fun PlaceholderItem() {
    Row (
        modifier = Modifier.fillMaxWidth().height(88.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(88.dp).shimmer()
        )

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
fun ExpenseFilterDialog() {

    rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
}



@Preview(
    showBackground = true
)
@Composable
fun ExpenseItemPreview() {
    ExpenseItem(Expense(
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
    ))
}