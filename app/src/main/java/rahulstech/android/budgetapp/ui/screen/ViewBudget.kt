package rahulstech.android.budgetapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices.PIXEL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.CategoryDialog
import rahulstech.android.budgetapp.ui.components.ExpenseDialog
import rahulstech.android.budgetapp.ui.components.ExpenseLinearProgress
import javax.inject.Inject

private const val TAG = "ViewBudget"

@HiltViewModel
class ViewBudgetViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {

    // observe budget
    private val _budgetState = MutableStateFlow<UIState<Budget>>(UIState.Idle())
    val budgetState = _budgetState.asStateFlow()

    fun observeBudget(id: String) {
        viewModelScope.launch {
            repo.observeBudgetById(id)
                .onStart { _budgetState.value = UIState.Loading() }
                .catch { cause -> _budgetState.value = UIState.Error(cause) }
                .collectLatest { budget ->
                    Log.i(TAG, "budget refreshed")
                    _budgetState.value = when(budget) {
                        null -> UIState.NotFound()
                        else -> UIState.Success(budget)
                    }
                }
        }
    }

    // edit budget
    private val actionEditBudget = UIAction<Budget,Budget>(
        action = { repo.editBudget(it) },
        converter = { arg,result ->
            if (null == result) {
                UIState.NotFound()
            }
            else {
                UIState.Success(result)
            }
        }
    )
    val budgetSaveState = actionEditBudget.uiState

    fun editBudget(budget: Budget) {
        viewModelScope.launch {
            actionEditBudget.doAction(budget)
        }
    }

    // add category
    private val actionAddCategory = UIAction<BudgetCategory, BudgetCategory>(
        action = { repo.addCategory(it) },
    )
    val categorySaveState = actionAddCategory.uiState

    fun addCategory(category: BudgetCategory) {
        viewModelScope.launch {
            actionAddCategory.doAction(category)
        }
    }

    // add expense
    private val actionAddExpense = UIAction<Expense,Expense>(
        action = { repo.addExpense(it) }
    )
    val expenseSaveState = actionAddExpense.uiState

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            actionAddExpense.doAction(expense)
        }
    }
}

@Composable
fun ViewBudgetRoute(budgetId: String,
                    snackBarCallback: SnackBarCallback,
                    navigateToCallback: NavigateToCallback,
                    exitScreenCallback: ExitScreenCallback,
                    viewModel: ViewBudgetViewModel = hiltViewModel(),
                    )
{
    val context = LocalContext.current

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
                onNavigateUp = {exitScreenCallback(null,null) },
                onAddExpense = { viewModel.addExpense(it) },
                onAddCategory = { viewModel.addCategory(it) },
                onEditBudget = { viewModel.editBudget(it) }
            )
        }
        is UIState.Error -> {
            Log.e(TAG, "view budget error", (budgetUIState as UIState.Error).cause)
            snackBarCallback(SnackBarEvent(
                message = stringResource(R.string.message_observe_budget_error),
                duration = SnackbarDuration.Short
            ))
            exitScreenCallback(null,null)
        }
        else -> {}
    }

    val budgetSaveState by viewModel.budgetSaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(budgetSaveState) {
        when(budgetSaveState) {
            is UIState.Success<Budget> -> {
                snackBarCallback(SnackBarEvent(
                    message = context.getString(R.string.message_budget_save_success)
                ))
            }
            else -> {}
        }
    }

    val categorySaveState by viewModel.categorySaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(categorySaveState) {
        when(categorySaveState) {
            is UIState.Success<BudgetCategory> -> {
                snackBarCallback(SnackBarEvent(
                    message = context.getString(R.string.message_budget_category_save_success)
                ))
            }
            is UIState.Error -> {
                Log.e(TAG,"category not saved", (categorySaveState as UIState.Error).cause)
                snackBarCallback(SnackBarEvent(
                    message = context.getString(R.string.message_budget_category_save_error)
                ))
            }
            else -> {}
        }
    }

    val expenseSaveState by viewModel.expenseSaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(expenseSaveState) {
        when(expenseSaveState) {
            is UIState.Success<Expense> -> {
                snackBarCallback(SnackBarEvent(
                    message = context.getString(R.string.message_expense_add_successful),
                ))
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBudgetScreen(budget: Budget,
                     onNavigateUp: ()-> Unit = {},
                     onAddExpense: (Expense) -> Unit = {},
                     onAddCategory: (BudgetCategory) -> Unit = {},
                     onEditBudget: (Budget)-> Unit = {},
                     )
{
    var showBudgetDetails by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    IconButton(onClick = { showBudgetDialog = true}) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.message_edit_category)
                        )
                    }

                    // info
                    IconButton(onClick = { showBudgetDetails = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.message_show_budget_details)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        BudgetContent(
            budget = budget,
            modifier = Modifier.padding(padding),
            categories = budget.categories,
            onAddExpense = onAddExpense,
            onAddCategory = {
                onAddCategory(it.copy(budgetId = budget.id))
            },
        )
    }

    if (showBudgetDetails) {
        BudgetDetailsDialog(
            details = budget.details,
            onDismiss = { showBudgetDetails = false }
        )
    }

    if (showBudgetDialog) {
        BudgetEditDialog(
            budget = budget,
            onDismiss = { showBudgetDialog = false },
            onClickSaveBudget = {
                showBudgetDialog = false
                onEditBudget(it.copy(id = budget.id))
            }
        )
    }
}

@Composable
private fun BudgetContent(budget: Budget,
                          modifier: Modifier = Modifier,
                          categories: List<BudgetCategory>,
                          onAddExpense: (Expense) -> Unit,
                          onAddCategory: (BudgetCategory)-> Unit,
                          )
{
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: Summary card (full width)
        item(span = { GridItemSpan(maxLineSpan) }) {
            BudgetSummaryCard(budget = budget)
        }

        // Header: Categories title row (full width)
        item(span = { GridItemSpan(maxLineSpan) }) {
            CategoriesHeader(
                onAddCategory = onAddCategory
            )
        }

        if (categories.isNotEmpty()) {
            // Grid items
            items(categories, key = { it.id }) { category ->
                CategoryCard(
                    category = category,
                    onAddExpense = onAddExpense,
                )
            }
        }
    }
}

@Composable
fun BudgetSummaryCard(budget: Budget) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
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
fun CategoriesHeader(onAddCategory: (BudgetCategory) -> Unit)
{
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.label_budget_categories),
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = { showAddCategoryDialog = true } ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }
    }

    if (showAddCategoryDialog) {
        CategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onClickSave = {
                showAddCategoryDialog = false
                onAddCategory(it)
            }
        )
    }
}

@Composable
fun CategoryCard(category: BudgetCategory,
                 onAddExpense: (Expense)-> Unit,
                 )
{
    var showAddExpenseDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
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
                    onClick = {  }
                ) {
                    Text(text = stringResource(R.string.label_view_budget_expenses))
                }

                // add expense
                FilledTonalButton (
                    onClick = { showAddExpenseDialog = true }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = stringResource(R.string.label_add_expense))
                }
            }


        }
    }

    if (showAddExpenseDialog) {
        ExpenseDialog(
            category = category,
            onDismiss = { showAddExpenseDialog = false },
            onSaveExpense = {
                showAddExpenseDialog = false
                onAddExpense(it)
            }
        )
    }
}

@Composable
fun BudgetDetailsDialog(details: String, onDismiss: () -> Unit)
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
fun BudgetEditDialog(budget: Budget,
                     onDismiss: ()-> Unit,
                     onClickSaveBudget: (Budget)-> Unit)
{
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    text = stringResource(R.string.title_edit_budget),
                    style = MaterialTheme.typography.titleLarge
                )

                // cancel
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.label_cancel))
                }

                // save
                TextButton(
                    enabled = name.isNotBlank(),
                    onClick = {
                        val newBudget = budget.copy(
                            name = name,
                            details = details
                        )
                        onClickSaveBudget(newBudget)
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


@Preview(
    showBackground = true,
    device = PIXEL,
)
@Composable
fun BudgetDetailsScreenPreview() {
    ViewBudgetScreen(
        budget = Budget(
            id = "1",
            name = "Darjeeling Tour",
            details = "Budget for Darjeeling tour of me and Rivu in February, 2026",
            totalExpense = 7000.0,
            totalAllocation = 15000.0,
            categories = listOf(
                BudgetCategory(
                    id = "1",
                    budgetId = "1",
                    name = "Travelling",
                    note = "All cost of travelling",
                    allocation = 9000.0,
                    totalExpense = 7200.0
                ),

                BudgetCategory(
                    id = "2",
                    budgetId = "1",
                    name = "This is a very long name of the category to test how long text it can contain",
                    note = "A very very very very very very very very very very very long note for the category to test how long text it  can fit",
                    allocation = 3000.0,
                    totalExpense = 2900.0
                ),

                BudgetCategory(
                    id = "3",
                    budgetId = "1",
                    name = "Hotels",
                    note = "All cost of hotels",
                    allocation = 7500.0,
                    totalExpense = 8000.0
                ),
            )
        )
    )
}

