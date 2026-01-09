package rahulstech.android.budgetapp.ui.screen.viewbudget

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.PIXEL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.BudgetDetailsDialog
import rahulstech.android.budgetapp.ui.components.BudgetEditDialog
import rahulstech.android.budgetapp.ui.components.CategoryDialog
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.components.BudgetDialogState
import rahulstech.android.budgetapp.ui.components.ExpenseDialog
import rahulstech.android.budgetapp.ui.components.ExpenseDialogState
import rahulstech.android.budgetapp.ui.components.ExpenseLinearProgress
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.Screen
import rahulstech.android.budgetapp.ui.screen.ScreenArgs
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.UIState

private const val TAG = "ViewBudget"


@Composable
fun ViewBudgetRoute(budgetId: Long,
                    snackBarCallback: SnackBarCallback,
                    navigateTo: NavigationCallback,
                    viewModel: ViewBudgetViewModel = hiltViewModel(),
                    )
{
    val context = LocalContext.current
    val budgetDialogState by viewModel.budgetEditDialogState.collectAsStateWithLifecycle()
    val categoryDialogState by viewModel.categoryDialogState.collectAsStateWithLifecycle()
    val expenseDialogState by viewModel.expenseDialogState.collectAsStateWithLifecycle()

    LaunchedEffect(budgetId) {
        viewModel.observeBudget(budgetId)
    }

    val budgetUIState by viewModel.budgetState.collectAsStateWithLifecycle()
    when(budgetUIState) {
        is UIState.Loading -> {
            // TODO: show loading
        }
        is UIState.Success<Budget> -> {
            val budget = (budgetUIState as UIState.Success<Budget>).data
            ViewBudgetScreen(
                budget = budget,
                navigateTo,
                onUIEvent = { event ->
                    when(event) {
                        is ViewBudgetUIEvent.ShowEditBudgetDialogEvent -> {
                            viewModel.updateBudgetEditDialogState(BudgetDialogState(showDialog = true, budget = budget))
                        }
                        is ViewBudgetUIEvent.EditBudgetEvent -> { viewModel.editBudget(event.budget) }
                        is ViewBudgetUIEvent.ShowAddCategoryDialogEvent -> {
                            viewModel.updateCategoryDialogState(BudgetCategoryDialogState(showDialog = true, budget = budget))
                        }
                        is ViewBudgetUIEvent.AddCategoryEvent -> { viewModel.addCategory(event.category) }
                        is ViewBudgetUIEvent.ShowAddExpenseDialogEvent -> {
                            viewModel.updateExpenseDialogState(ExpenseDialogState(showDialog = true, category = event.category))
                        }
                        is ViewBudgetUIEvent.AddExpenseEvent -> { viewModel.addExpense(event.expense) }
                        is ViewBudgetUIEvent.ViewExpenses -> {
                            navigateTo(NavigationEvent.ForwardTo(
                                screen = Screen.ViewExpenses,
                                args = ScreenArgs(budgetId = event.budgetId, categoryId = event.categoryId)
                            ))
                        }
                    }
                }
            )
        }
        is UIState.NotFound -> {
            Log.i(TAG,"budget with id=$budgetId not found")
            snackBarCallback(SnackBarEvent(
                message = stringResource(R.string.message_budget_not_found)
            ))
            navigateTo(NavigationEvent.Exit())
        }
        is UIState.Error -> {
            Log.e(TAG, "view budget error", (budgetUIState as UIState.Error).cause)
            snackBarCallback(
                SnackBarEvent(
                    message = stringResource(R.string.message_observe_budget_error),
                    duration = SnackbarDuration.Short
                )
            )
            navigateTo(NavigationEvent.Exit())
        }
        else -> {}
    }

    val budgetSaveState by viewModel.budgetSaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(budgetSaveState) {
        when(budgetSaveState) {
            is UIState.Success<Budget> -> {
                viewModel.updateBudgetEditDialogState(BudgetDialogState())
                snackBarCallback(
                    SnackBarEvent(
                        message = context.getString(R.string.message_budget_save_success)
                    )
                )
            }
            else -> {}
        }
    }

    if (budgetDialogState.showDialog) {
        BudgetEditDialog(
            budgetDialogState = budgetDialogState,
            onDismiss = { viewModel.updateBudgetEditDialogState(BudgetDialogState()) },
            onClickSave = { editedBudget ->
                // BudgetEditDialog already set the id to the editedBudget
                viewModel.updateBudgetEditDialogState(budgetDialogState.copy(enabled = false, budget = editedBudget))
                viewModel.editBudget(editedBudget)
            }
        )
    }

    val categorySaveState by viewModel.categorySaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(categorySaveState) {
        when(categorySaveState) {
            is UIState.Success<BudgetCategory> -> {
                viewModel.updateCategoryDialogState(BudgetCategoryDialogState())
                snackBarCallback(
                    SnackBarEvent(
                        message = context.getString(R.string.message_budget_category_save_success)
                    )
                )
            }
            is UIState.Error -> {
                Log.e(TAG,"category not saved", (categorySaveState as UIState.Error).cause)
                snackBarCallback(
                    SnackBarEvent(
                        message = context.getString(R.string.message_budget_category_save_error)
                    )
                )
            }
            else -> {}
        }
    }

    if (categoryDialogState.showDialog) {
        CategoryDialog(
            categoryDialogState = categoryDialogState,
            onDismiss = { viewModel.updateCategoryDialogState(BudgetCategoryDialogState()) },
            onClickSave = { category ->
                // CategoryDialog already set the budgetId to the category
                viewModel.updateCategoryDialogState(categoryDialogState.copy(isSaving = true, category = category))
                viewModel.addCategory(category)
            }
        )
    }

    val expenseSaveState by viewModel.expenseSaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(expenseSaveState) {
        when(expenseSaveState) {
            is UIState.Success<Expense> -> {
                viewModel.updateExpenseDialogState(ExpenseDialogState())
                snackBarCallback(
                    SnackBarEvent(
                        message = context.getString(R.string.message_expense_add_successful),
                    )
                )
            }
            else -> {}
        }
    }

    if (expenseDialogState.showDialog) {
        ExpenseDialog(
            expenseDialogState = expenseDialogState,
            onDismiss = { viewModel.updateExpenseDialogState(ExpenseDialogState()) },
            onSaveExpense = { expense ->
                // ExpenseDialog already set the budgetId and categoryId to the expense
                viewModel.updateExpenseDialogState(expenseDialogState.copy(isSaving = true, expense = expense))
                viewModel.addExpense(expense)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBudgetScreen(budget: Budget,
                     navigateTo: NavigationCallback = {},
                     onUIEvent: ViewBudgetUIEventCallback = {}
                     )
{
    var showBudgetDetails by remember { mutableStateOf(false) }
    val categories = budget.categories

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = budget.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    // edit
                    IconButton(onClick = { onUIEvent(ViewBudgetUIEvent.ShowEditBudgetDialogEvent(budget)) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.message_edit_category)
                        )
                    }

                    // info
                    IconButton(onClick = { showBudgetDetails = true }) {
                        Icon(Icons.Outlined.Info, stringResource(R.string.message_show_budget_details))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigateTo(NavigationEvent.Exit()) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.message_navigate_up))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header: Summary card (full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                BudgetSummaryCard(budget = budget)
            }

            // Header: Categories title row (full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoriesHeader(
                    onClickAddCategory = { onUIEvent(
                        ViewBudgetUIEvent.ShowAddCategoryDialogEvent(
                            budget
                        )
                    ) }
                )
            }

            if (categories.isNotEmpty()) {
                // Grid items
                items(categories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onUIEvent = onUIEvent,
                    )
                }
            }
        }
    }

    if (showBudgetDetails) {
        BudgetDetailsDialog(
            details = budget.details,
            onDismiss = { showBudgetDetails = false }
        )
    }
}

@Composable
fun BudgetSummaryCard(budget: Budget) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            ExpenseLinearProgress(
                expense = budget.totalExpense,
                allocation = budget.totalAllocation,
                barThickness = 16.dp,
                textSize = 20.sp,
                labelExpense = stringResource(R.string.label_total_expense),
                labelAllocation = stringResource(R.string.label_total_allocation)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.End)
            ) {
                FilledTonalButton(onClick = {}) {
                    Text(text = stringResource(R.string.label_view_budget_expenses))
                }
            }
        }
    }
}

@Composable
fun CategoriesHeader(onClickAddCategory: ()-> Unit)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.label_budget_categories),
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = onClickAddCategory) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
fun CategoryCard(category: BudgetCategory,
                 onUIEvent: ViewBudgetUIEventCallback,
                 )
{
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.note,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(12.dp))

            ExpenseLinearProgress(
                expense = category.totalExpense,
                allocation = category.allocation,
                labelExpense = stringResource(R.string.label_expense),
                labelAllocation = stringResource(R.string.label_allocation),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.End)
            ) {
                // view expenses
                FilledTonalButton (
                    onClick = { onUIEvent(ViewBudgetUIEvent.ViewExpenses(budgetId = category.budgetId, categoryId = category.id)) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_currency_rupee_24),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = stringResource(R.string.label_view_budget_expenses))
                }

                // add expense
                FilledTonalButton (
                    onClick = { onUIEvent(ViewBudgetUIEvent.ShowAddExpenseDialogEvent(category)) }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = stringResource(R.string.label_add_expense))
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = PIXEL,
)
@Composable
fun BudgetDetailsScreenPreview() {
    ViewBudgetScreen(
        budget = Budget(
            id = 1,
            name = "Darjeeling Tour",
            details = "Budget for Darjeeling tour of me and Rivu in February, 2026",
            totalExpense = 7000.0,
            totalAllocation = 15000.0,
            categories = listOf(
                BudgetCategory(
                    id = 1,
                    budgetId = 1,
                    name = "Travelling",
                    note = "All cost of travelling",
                    allocation = 9000.0,
                    totalExpense = 7200.0
                ),

                BudgetCategory(
                    id = 2,
                    budgetId = 1,
                    name = "This is a very long name of the category to test how long text it can contain",
                    note = "A very very very very very very very very very very very long note for the category to test how long text it  can fit",
                    allocation = 3000.0,
                    totalExpense = 2900.0
                ),

                BudgetCategory(
                    id = 3,
                    budgetId = 1,
                    name = "Hotels",
                    note = "All cost of hotels",
                    allocation = 7500.0,
                    totalExpense = 8000.0
                ),
            )
        )
    )
}

