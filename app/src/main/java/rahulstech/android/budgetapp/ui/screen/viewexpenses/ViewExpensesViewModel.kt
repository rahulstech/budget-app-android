package rahulstech.android.budgetapp.ui.screen.viewexpenses

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import rahulstech.android.budgetapp.repository.BudgetRepository
import javax.inject.Inject

@HiltViewModel
class ViewExpensesViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {


}