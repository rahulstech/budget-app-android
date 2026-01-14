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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import rahulstech.android.budgetapp.ui.components.DeleteCategoryWarningDialog
import rahulstech.android.budgetapp.ui.components.ExpenseDialog
import rahulstech.android.budgetapp.ui.components.ExpenseLinearProgress
import rahulstech.android.budgetapp.ui.components.OptionItem
import rahulstech.android.budgetapp.ui.components.OptionsDialog
import rahulstech.android.budgetapp.ui.components.shimmer
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.ScaffoldState
import rahulstech.android.budgetapp.ui.screen.ScaffoldStateCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.TopBarAction
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIState
import rahulstech.android.budgetapp.ui.screen.UIText
import rahulstech.android.budgetapp.ui.theme.tileColors

private const val TAG = "ViewBudget"

@Composable
fun ViewBudgetRoute(budgetId: Long,
                    scaffoldStateCallback: ScaffoldStateCallback,
                    snackBarCallback: SnackBarCallback,
                    navigationCallback: NavigationCallback,
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
                    navigationCallback(effect.event)
                }
                is UISideEffect.ExitScreen -> {
                    navigationCallback(NavigationEvent.Exit())
                }
            }
        }
    }

    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val budgetState = uiState.budgetState

    when(budgetState) {
        is UIState.NotFound -> {
            Log.i(TAG,"budget not found")
            scaffoldStateCallback(ScaffoldState())
            snackBarCallback(SnackBarEvent(message = stringResource(R.string.message_budget_not_found)))
            navigationCallback(NavigationEvent.Exit())
        }
        is UIState.Error -> {
            Log.e(TAG, "view budget error", (uiState.budgetState as UIState.Error).cause)
            scaffoldStateCallback(ScaffoldState())
            snackBarCallback(SnackBarEvent(message = stringResource(R.string.message_observe_budget_error)))
            navigationCallback(NavigationEvent.Exit())
        }
        is UIState.Success -> {
            val budget = (uiState.budgetState as UIState.Success<Budget>).data
            scaffoldStateCallback(ScaffoldState(
                showNavUp = true,
                title = budget.name,
                actions = listOf(
                    // budget details
                    TopBarAction.TextAction(stringResource(R.string.label_budget_details),
                        { viewModel.showBudgetDetailsDialog(budget) }),

                    // edit budget
                    TopBarAction.TextAction(stringResource(R.string.label_edit),
                        { viewModel.showEditBudgetDialog(budget) }),

                    // delete budget
                    TopBarAction.TextAction(stringResource(R.string.label_delete),
                        { viewModel.showDeleteBudgetDialog(budget) }),
                ),
            ))

            ViewBudgetScreen(
                budgetState = uiState.budgetState,
                categoriesState = uiState.categoryState,
                onUIEvent = viewModel::onUIEvent,
            )
        }
        else -> {
            scaffoldStateCallback(ScaffoldState())
        }
    }

    if (uiState.editBudgetDialog.showDialog) {
        BudgetEditDialog(
            budgetDialogState = uiState.editBudgetDialog,
            onDismiss = { viewModel.hideEditBudgetDialog() },
            onClickSave = { viewModel.editBudget(it) }
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
            categoryDialogState = uiState.editCategoryDialog,
            onDismiss = { viewModel.hideEditCategoryDialog() },
            onClickSave = { viewModel.editCategory(it) }
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
                     onUIEvent: ViewBudgetUIEventCallback = {},
                     )
{
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        when(budgetState) {
            is UIState.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(shape = RoundedCornerShape(16.dp))
                        .shimmer()
                    )
                }
            }
            is UIState.Success -> {
                val budget = budgetState.data

                // Header: Summary card (full width)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BudgetSummaryCard(budget)
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
                    Box(modifier = Modifier
                        .size(width = 320.dp, height = 300.dp)
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

@Composable
fun BudgetSummaryCard(budget: Budget)
{
    Card(
        colors = tileColors(),
        shape = MaterialTheme.shapes.medium,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                FilledTonalIconButton(onClick = { onUIEvent(ViewBudgetUIEvent.ShowCategoryOptionsEvent(category)) }) {
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
                    Icon(Icons.AutoMirrored.Default.List,
                        contentDescription = stringResource(R.string.message_view_expenses))
                }

                // add expense
                FilledTonalIconButton(
                    onClick = { onUIEvent(ViewBudgetUIEvent.ShowAddExpenseDialogEvent(category)) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.message_add_expense))
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
    OptionsDialog(
        onDismiss = onDismiss,
        options = arrayOf(
            OptionItem(stringResource(R.string.label_edit), { onUIEvent(ViewBudgetUIEvent.ShowEditCategoryDialogEvent(category)) }),
            OptionItem(stringResource(R.string.label_delete), { onUIEvent(ViewBudgetUIEvent.ShowDeleteCategoryDialogEvent(category)) })
        )
    )
}

