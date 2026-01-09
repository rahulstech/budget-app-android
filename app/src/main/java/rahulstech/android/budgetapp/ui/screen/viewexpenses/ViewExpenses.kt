package rahulstech.android.budgetapp.ui.screen.viewexpenses

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import java.time.LocalDate

@Composable
fun ViewExpensesRoute(budgetId: Long,
                      categoryId: Long? = null,
                      snackBarCallback: SnackBarCallback,
                      navigateTo: NavigationCallback,
                      viewModel: ViewExpensesViewModel = hiltViewModel())
{
    ViewExpensesScreen(navigateTo = navigateTo)
}

@Composable
fun ViewExpensesScreen(navigateTo: NavigationCallback) {
    Scaffold(
        topBar = {
            TopAppBar(
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
                            stringResource(R.string.mesage_filter_expenses))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigateTo(NavigationEvent.Exit()) }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.message_navigate_up)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: handle add expense click */ },
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {

        }
    }
}

@Composable
fun StickyExpenseHeader(date: LocalDate) {
    Surface(
        tonalElevation = 4.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.DateRange, contentDescription = null)

            Text(text = date.toString()) // TODO: formate expense header date
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    Column {
        Text(
            text = expense.amount.toString(),
            style = MaterialTheme.typography.titleLarge,
        )

        if (expense.note.isNotBlank()) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = expense.note,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SuggestionChip(
            onClick = {},
            label = {
                Text(text = "")
            },
        )
    }
}