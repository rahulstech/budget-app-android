package rahulstech.android.budgetapp.ui.screen.viewbudget

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.components.BudgetDetailsDialog
import rahulstech.android.budgetapp.ui.components.BudgetEditDialog
import rahulstech.android.budgetapp.ui.components.CategoryDialog
import rahulstech.android.budgetapp.ui.components.DeleteBudgetWarningDialog
import rahulstech.android.budgetapp.ui.components.ExpenseDialog
import rahulstech.android.budgetapp.ui.components.ExpenseLinearProgress
import rahulstech.android.budgetapp.ui.components.shimmer
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIState
import rahulstech.android.budgetapp.ui.screen.UIText
import rahulstech.android.budgetapp.ui.theme.primaryTopAppBarColors
import rahulstech.android.budgetapp.ui.theme.tileColors

private const val TAG = "ViewBudget"

@Composable
fun ViewBudgetRoute(budgetId: Long,
                    snackBarCallback: SnackBarCallback,
                    navigateTo: NavigationCallback,
                    viewModel: ViewBudgetViewModel = hiltViewModel(),
                    )
{
    val context = LocalContext.current

    LaunchedEffect(budgetId) {
        viewModel.observeBudget(budgetId)
    }

    LaunchedEffect(Unit) {
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

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    when(uiState.budgetState) {
        is UIState.NotFound -> {
            Log.i(TAG,"budget not found")
            snackBarCallback(SnackBarEvent(message = stringResource(R.string.message_budget_not_found)))
            navigateTo(NavigationEvent.Exit())
        }
        is UIState.Error -> {
            Log.e(TAG, "view budget error", (uiState.budgetState as UIState.Error).cause)
            snackBarCallback(SnackBarEvent(
                    message = stringResource(R.string.message_observe_budget_error),
                    duration = SnackbarDuration.Short
                )
            )
            navigateTo(NavigationEvent.Exit())
        }
        is UIState.Success -> {
            ViewBudgetScreen(
                budgetState = uiState.budgetState,
                categoriesState = uiState.categoryState,
                navigateTo = navigateTo,
                onUIEvent = viewModel::onUIEvent
            )
        }
        else -> {}
    }

    if (uiState.editBudgetDialog.showDialog) {
        BudgetEditDialog(
            budgetDialogState = uiState.editBudgetDialog,
            onDismiss = { viewModel.hideEditBudgetDialog() },
            onClickSave = { budget ->
                // BudgetEditDialog already set the id to the editedBudget
                viewModel.updateEditBudgetDialog(true, budget)
                viewModel.editBudget(budget)
            }
        )
    }

    if (uiState.addCategoryDialog.showDialog) {
        CategoryDialog(
            categoryDialogState = uiState.addCategoryDialog,
            onDismiss = { viewModel.hideAddCategoryDialog() },
            onClickSave = { viewModel.addCategory(it) }
        )
    }

    if (uiState.addExpenseDialog.showDialog) {
        ExpenseDialog(
            expenseDialogState = uiState.addExpenseDialog,
            onDismiss = { viewModel.hideAddExpenseDialog() },
            onSaveExpense = { viewModel.addExpense(it) }
        )
    }

    if (uiState.deleteBudgetDialog.showDialog) {
        DeleteBudgetWarningDialog(
            budget = uiState.deleteBudgetDialog.budget,
            onDismiss = { viewModel.hideDeleteBudgetDialog() },
            onClickDelete = { budget ->
                viewModel.hideDeleteBudgetDialog()
                viewModel.removeBudget(budget)
            }
        )
    }

    if (uiState.budgetDetailsDialog.showDialog) {
        BudgetDetailsDialog(
            details = uiState.budgetDetailsDialog.budget.details,
            onDismiss = { viewModel.hideBudgetDetailsDialog() }
        )
    }

    if (uiState.categoryOptionsDialog.showDialog) {
        CategoryOptionDialog(
            category = uiState.categoryOptionsDialog.category,
            onDismiss = { viewModel.hideCategoryOptionsDialog() },
            onUIEvent = viewModel::onUIEvent,
        )
    }

    if (uiState.editCategoryDialog.showDialog) {
        CategoryDialog(
            categoryDialogState = uiState.categoryOptionsDialog,
            onDismiss = { viewModel.hideCategoryOptionsDialog() },
            onClickSave = {
                viewModel.editCategory(it)
            }
        )
    }

    if (uiState.deleteCategoryDialog.showDialog) {
        DeleteCategoryWarningDialog(
            category = uiState.deleteCategoryDialog.category,
            onDismiss = { viewModel.hideDeleteCategoryDialog() },
            onClickDelete = {
                viewModel.hideDeleteCategoryDialog()
                viewModel.removeCategory(it)
            }
        )
    }
}

@Composable
fun ViewBudgetScreen(budgetState: UIState<Budget>,
                     categoriesState: UIState<List<BudgetCategory>>,
                     navigateTo: NavigationCallback = {},
                     onUIEvent: ViewBudgetUIEventCallback = {},
                     )
{
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        topBar = {
            TopAppBar(
                colors = primaryTopAppBarColors(),

                title = {
                    when(budgetState) {
                        is UIState.Loading -> {
                            Box(modifier = Modifier.size(width = 100.dp, height = 36.dp).shimmer())
                        }
                        is UIState.Success -> {
                            Text(
                                text = budgetState.data.name,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                        else -> {}
                    }
                },

                actions = {
                    if (budgetState is UIState.Success) {
                        val budget = budgetState.data

                        // delete
                        IconButton(onClick = { onUIEvent(ViewBudgetUIEvent.DeleteBudgetEvent(budget)) }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.message_delete_budget))
                        }

                        // edit
                        IconButton(onClick = { onUIEvent(ViewBudgetUIEvent.ShowEditBudgetDialogEvent(budget)) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.message_edit_category)
                            )
                        }

                        // info
                        IconButton(
                            onClick = {
                                onUIEvent(ViewBudgetUIEvent.ShowBudgetDetailsEvent(budgetState.data))
                            }
                        ) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.message_show_budget_details))
                        }
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

            when(budgetState) {
                is UIState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.fillMaxWidth().height(260.dp)
                            .clip(shape = RoundedCornerShape(16.dp))
                            .shimmer()
                        )
                    }
                }
                is UIState.Success -> {
                    val budget = budgetState.data

                    // Header: Summary card (full width)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        BudgetSummaryCard(budget, onUIEvent)
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
                }
                else -> {}
            }

            when(categoriesState) {
                is UIState.Loading -> {
                    items(10) {
                        Box(modifier = Modifier.size(width = 320.dp, height = 300.dp)
                            .clip(shape = RoundedCornerShape(16.dp))
                            .shimmer()
                        )
                    }
                }
                is UIState.Success -> {
                    val categories = categoriesState.data

                    // Grid items
                    items(items = categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            onUIEvent = onUIEvent,
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun BudgetSummaryCard(budget: Budget,
                      onUIEvent: ViewBudgetUIEventCallback)
{
    Card(
        colors = tileColors(),
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
                FilledTonalButton(onClick = { onUIEvent(ViewBudgetUIEvent.ViewExpensesEvent(budget.id))}) {
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
        colors = tileColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                IconButton(onClick = { onUIEvent(ViewBudgetUIEvent.ShowCategoryOptionsEvent(category)) }) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.message_show_more_options))
                }
            }

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
                    onClick = { onUIEvent(ViewBudgetUIEvent.ViewExpensesEvent(budgetId = category.budgetId, categoryId = category.id)) }
                ) {
                    Text(text = stringResource(R.string.label_view_budget_expenses))
                }

                // add expense
                FilledTonalButton (
                    onClick = { onUIEvent(ViewBudgetUIEvent.ShowAddExpenseDialogEvent(category)) }
                ) {
                    Text(text = stringResource(R.string.label_add_expense))
                }
            }
        }
    }
}

@Composable
fun CategoryOptionDialog(category: BudgetCategory,
                         onDismiss: ()-> Unit,
                         onUIEvent: ViewBudgetUIEventCallback)
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
                    onClick = { onUIEvent(ViewBudgetUIEvent.ShowEditCategoryDialogEvent(category)) },

                ) {
                    Text(stringResource(R.string.label_edit), style = MaterialTheme.typography.bodyLarge)
                }

                // delete
                TextButton(modifier = Modifier.fillMaxWidth().height(56.dp),
                    onClick = { onUIEvent(ViewBudgetUIEvent.ShowDeleteCategoryDialogEvent(category)) },
                ) {
                    Text(stringResource(R.string.label_delete), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun DeleteCategoryWarningDialog(category: BudgetCategory,
                                onDismiss: ()-> Unit,
                                onClickDelete: (BudgetCategory)-> Unit)
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

                Text(text = stringResource(R.string.message_warning_delete_budget_category, category.name))

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    // yes
                    TextButton(onClick = { onClickDelete(category) }) { Text(stringResource(R.string.label_yes))}

                    // no
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.label_no)) }
                }
            }
        }
    }
}