package rahulstech.android.budgetapp.ui.screen.budgetlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.ui.components.ExpenseLinearProgress
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.Screen
import rahulstech.android.budgetapp.ui.screen.ScreenArgs
import javax.inject.Inject

@Composable
fun BudgetListRoute(navigateTo: NavigationCallback,
                    viewModel: BudgetListViewMode = hiltViewModel(),
                    )
{

    val budgets by viewModel.allBudgets.collectAsStateWithLifecycle(emptyList())
    BudgetListScreen(
        budgets = budgets,
        onClickBudget = { navigateTo(
            NavigationEvent.ForwardTo(Screen.ViewBudget, ScreenArgs(budgetId = it.id))
        )},
        onClickCreateBudget = { navigateTo(NavigationEvent.ForwardTo(Screen.CreateBudget))},
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    budgets: List<Budget>,
    onClickBudget: (Budget) -> Unit,
    onClickCreateBudget: ()-> Unit,
) {
    Scaffold(
        // TODO: set container color
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_budget_list),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
            )
        },
        floatingActionButton = {
            Button(
                onClick = onClickCreateBudget
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = stringResource(R.string.label_create_budget))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyVerticalGrid(
            modifier = Modifier.padding(padding),
            columns = GridCells.Adaptive(minSize = 260.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = budgets,
                key = { it.id }
            ) { budget ->
                BudgetListItem(
                    budget = budget,
                    onClickBudget = onClickBudget
                )
            }
        }
    }

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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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

@Preview(
    showBackground = true,
    widthDp = 280,
)
@Composable
fun BudgetListScreenPreview() {
    BudgetListScreen(
        budgets = listOf(
            Budget(
                id = 1,
                name = "Travel to Darjeeling",
                totalExpense = 1500.0,
                totalAllocation = 2000.0
            ),

            Budget(
                id = 2,
                name = "Travel to Mandarmanee",
                totalExpense = 1600.0,
                totalAllocation = 3600.0
            ),

            Budget(
                id = 3,
                name = "Travel to Gangotree",
                totalExpense = 4500.0,
                totalAllocation = 3000.0
            ),

            Budget(
                id = 4,
                name = "Travel to Gangtok",
                totalExpense = 0.0,
                totalAllocation = 3000.0
            )
        ),
        onClickBudget = {},
        onClickCreateBudget = {}
    )
}