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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import rahulstech.android.budgetapp.ui.components.ExpenseDialog
import rahulstech.android.budgetapp.ui.components.shimmer
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIText
import rahulstech.android.budgetapp.ui.theme.primaryTopAppBarColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "ViewExpenses"

private val EXPENSE_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd-MMMM-yyyy")

@Composable
fun ViewExpensesRoute(budgetId: Long,
                      categoryId: Long? = null,
                      snackBarCallback: SnackBarCallback,
                      navigateTo: NavigationCallback,
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
                    navigateTo(effect.event)
                }
                is UISideEffect.ExitScreen -> {
                    navigateTo(NavigationEvent.Exit())
                }
            }
        }
    }

    val expenses = viewModel.expenses.collectAsLazyPagingItems()
    ViewExpensesScreen(
        expenses = expenses,
        navigateTo = navigateTo,
        onClickAddExpense = { viewModel.showAddExpenseDialog(budgetId) },
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
                       navigateTo: NavigationCallback = {},
                       onClickAddExpense: ()-> Unit = {},
                       onUIEvent: ViewExpenseUIEventCallback = {}
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
            Text(
                modifier = Modifier.fillMaxWidth().weight(1f),
                text = expense.amount.toString(),
                style = MaterialTheme.typography.titleLarge,
            )

            IconButton(onClick = { onClickOptions(expense) }) {
                Icon(Icons.Default.MoreVert, stringResource(R.string.message_show_more_options))
            }
        }

        if (expense.note.isNotBlank()) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = expense.note,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            modifier = Modifier.clip(shape = MaterialTheme.shapes.extraLarge).background(MaterialTheme.colorScheme.secondaryContainer)

                .padding(horizontal = 12.dp, vertical = 8.dp)
                ,
            text = expense.category!!.name,
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider()
    }
}

@Composable
fun PlaceholderItem() {
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
                        onUIEvent: ViewExpenseUIEventCallback
                         )
{
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Surface(
            tonalElevation = 0.dp,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // edit
                TextButton(modifier = Modifier.fillMaxWidth().height(56.dp),
                    onClick = { onUIEvent(ViewExpenseUIEvent.ShowEditExpenseDialogEvent(expense)) },

                    ) {
                    Text(stringResource(R.string.label_edit), style = MaterialTheme.typography.bodyLarge)
                }

                // delete
                TextButton(modifier = Modifier.fillMaxWidth().height(56.dp),
                    onClick = { onUIEvent(ViewExpenseUIEvent.ShowDeleteExpenseDialogEvent(expense)) },
                ) {
                    Text(stringResource(R.string.label_delete), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun DeleteExpenseWarningDialog(expense: Expense,
                               onDismiss: ()-> Unit,
                               onClickDelete: (Expense)-> Unit)
{
    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.clip(MaterialTheme.shapes.large),
        ) {
            Column(
                modifier = Modifier.wrapContentSize().padding(16.dp)
            ) {
                Text(text = stringResource(R.string.title_delete_expense), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(R.string.message_warning_delete_expense))

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    // yes
                    TextButton(onClick = { onClickDelete(expense) }) { Text(stringResource(R.string.label_yes))}

                    // no
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.label_no)) }
                }
            }
        }
    }
}


@Preview(
    showBackground = true
)
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