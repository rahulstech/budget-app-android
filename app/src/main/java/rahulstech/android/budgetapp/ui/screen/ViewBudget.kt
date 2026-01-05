package rahulstech.android.budgetapp.ui.screen

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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
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
import rahulstech.android.budgetapp.ui.widget.ExpenseLinearProgress
import javax.inject.Inject

private const val TAG = "ViewBudget"

@HiltViewModel
class ViewBudgetViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {

    private val _budgetState = MutableStateFlow<UIState<Budget>>(UIState.Idle())
    val budgetState = _budgetState.asStateFlow()

    fun observeBudget(id: String) {
        viewModelScope.launch {
            repo.observeBudgetById(id)
                .onStart { _budgetState.value = UIState.Loading() }
                .catch { cause -> UIState.Error(cause) }
                .collectLatest { budget ->
                    if (null == budget) {
                        _budgetState.value = UIState.NotFound()
                    }
                    else {
                        _budgetState.value = UIState.Success(budget)
                    }
                }
        }
    }
}

@Composable
fun ViewBudgetRoute(budgetId: String,
                    snackBarCallback: SnackBarCallback,
                    exitScreenCallback: ExitScreenCallback,
                    viewModel: ViewBudgetViewModel = hiltViewModel(),
                    )
{
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
            BudgetDetailsScreen(
                budget = budget,
                onNavigateUp = {exitScreenCallback(null,null)}
            )
        }
        is UIState.Error -> {
            Log.e(TAG, "view budget error", (budgetUIState as UIState.Error).cause)
            // TODO: show SnackBar and exit
            snackBarCallback(SnackBarEvent(
                message = "Something error occurred"
            ))
            exitScreenCallback(null,null)
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailsScreen(budget: Budget,
                        onNavigateUp: ()-> Unit = {},
                        ) {
    var showBudgetDetails by remember { mutableStateOf(false) }

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
            categories = budget.categories
        )
    }

    if (showBudgetDetails) {
        // TODO: show dialog
    }
}

@Composable
private fun BudgetContent(budget: Budget,
                          modifier: Modifier = Modifier,
                          categories: List<BudgetCategory>,
                          )
{
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 240.dp),
        modifier = modifier.fillMaxSize()
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
                onClickAddNewCategory = {}
            )
        }

        if (categories.isNotEmpty()) {

            // Grid items
            items(categories, key = { it.id }) { category ->
                CategoryCard(
                    category = category,
                    onClickEditCategory = { }
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
        Box(
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
        }
    }
}

@Composable
fun CategoriesHeader(onClickAddNewCategory: ()->Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.label_budget_categories),
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(onClick = onClickAddNewCategory ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
fun CategoryCard(category: BudgetCategory,
                 onClickEditCategory: (BudgetCategory)-> Unit,
                 )
{
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
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = category.note,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                FilledTonalIconButton (
                    onClick = { onClickEditCategory(category) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.message_edit_category, category.name)
                    )
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

            FilledTonalButton (
                modifier = Modifier.align(Alignment.End),
                onClick = { }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = stringResource(R.string.label_add_expense))
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
    BudgetDetailsScreen(
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

