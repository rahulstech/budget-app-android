package rahulstech.android.budgetapp.ui.screen.budgetlist

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import javax.inject.Inject


@HiltViewModel
class BudgetListViewMode @Inject constructor(private val repo: BudgetRepository): ViewModel() {

    val allBudgets: Flow<List<Budget>> by lazy { repo.observeAllBudgets() }
}
