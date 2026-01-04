package rahulstech.android.budgetapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import rahulstech.android.budgetapp.repository.BudgetRepository
import javax.inject.Inject

@HiltViewModel
class ViewBudgetViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {

}

@Composable
fun ViewBudgetRoute(budgetId: String) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBudgetScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "View Budget",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {  }
    }
}