package rahulstech.android.budgetapp.ui.screen.budgetlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.ui.components.ExpenseLinearProgress
import rahulstech.android.budgetapp.ui.components.shimmer
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.Screen
import rahulstech.android.budgetapp.ui.screen.ScreenArgs
import rahulstech.android.budgetapp.ui.theme.primaryTopAppBarColors
import rahulstech.android.budgetapp.ui.theme.tileColors

@Composable
fun BudgetListRoute(navigateTo: NavigationCallback,
                    viewModel: BudgetListViewMode = hiltViewModel(),
                    )
{
    val budgets = viewModel.allBudgets.collectAsLazyPagingItems()
    BudgetListScreen(
        budgets = budgets,
        onClickBudget = {
            navigateTo(NavigationEvent.ForwardTo(Screen.ViewBudget, ScreenArgs(budgetId = it.id)))
        },
        onClickCreateBudget = { navigateTo(NavigationEvent.ForwardTo(Screen.CreateBudget))},
    )
}

@Composable
fun BudgetListScreen(budgets: LazyPagingItems<Budget>,
                     onClickBudget: (Budget) -> Unit,
                     onClickCreateBudget: ()-> Unit,
                     )
{
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        topBar = {
            TopAppBar(
                colors = primaryTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(R.string.title_budget_list),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton (
                shape = RoundedCornerShape(percent = 50),
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = onClickCreateBudget
            ) {
                Icon(Icons.Default.Add, null)

                Spacer(modifier = Modifier.width(12.dp))

                Text(text = stringResource(R.string.label_create_budget))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 260.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top)
            ) {
                when(budgets.loadState.prepend) {
                    is LoadState.Loading -> {
                        item {
                            BudgetListPlaceholderItem()
                        }
                    }
                    else -> {}
                }

                when(budgets.loadState.refresh) {
                    is LoadState.Loading -> {
                        items(10) {
                            BudgetListPlaceholderItem()
                        }
                    }
                    is LoadState.Error -> {
                        // TODO: show budget loading error
                    }
                    is LoadState.NotLoading -> {
                        if (budgets.itemCount == 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyView()
                            }
                        }
                        else {
                            items(
                                count = budgets.itemCount,
                                key = { index -> budgets[index]!!.id }
                            ) { index ->
                                budgets[index]?.let { budget ->
                                    BudgetListItem(
                                        budget = budget,
                                        onClickBudget = onClickBudget
                                    )
                                }
                            }
                        }
                    }
                }

                when(budgets.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            BudgetListPlaceholderItem()
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}




@Composable
fun BudgetListPlaceholderItem() {
    Box(modifier = Modifier.size(width = 260.dp, height = 150.dp)
        .clip(shape = RoundedCornerShape(16.dp))
        .shimmer()
    )
}

@Composable
fun BudgetListItem(
    budget: Budget,
    onClickBudget: (Budget) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable { onClickBudget(budget) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = tileColors(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Title
            Text(
                text = budget.name,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            ExpenseLinearProgress(
                expense = budget.totalExpense,
                allocation = budget.totalAllocation,
                labelExpense = stringResource(R.string.label_expense),
                labelAllocation = stringResource(R.string.label_allocation)
            )
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxWidth().height(500.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(R.string.label_no_budget), style = MaterialTheme.typography.titleLarge)
    }
}