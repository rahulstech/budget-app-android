package rahulstech.android.budgetapp.ui.screen.viewexpenses

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.ExpenseFilterParams
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.ExpenseDialogStateManager
import rahulstech.android.budgetapp.ui.screen.UIAction
import javax.inject.Inject

private const val TAG = "ViewExpensesViewModel"

@HiltViewModel
class ViewExpensesViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {


    private val _expenseFilterParamsState = MutableStateFlow(ExpenseFilterParams(0))

    val expenses: Flow<PagingData<ExpenseListItem>> =
        _expenseFilterParamsState.flatMapLatest { params ->
            Log.i(TAG, "ExpenseFilterParams=$params")
                repo.observeExpenses(params)
                    .map { pagingData ->
                        Log.i(TAG, "Expenses PagingSource loading complete")
                        pagingData
                            .map { ExpenseListItem.ItemExpense(it) }
                            .insertSeparators { before, after ->
                                val afterDate = after?.value?.date
                                val beforeDate = before?.value?.date

                                if (afterDate != null && beforeDate != afterDate) {
                                    ExpenseListItem.ItemHeader(afterDate)
                                }
                                else null
                            }
                    }
            }
            .cachedIn(viewModelScope)

    fun filterExpenses(params: ExpenseFilterParams) {
        _expenseFilterParamsState.value = params
    }

    // add expense
    private val _actionAddExpense = UIAction<Expense, Expense>(
        action = { repo.addExpense(it) }
    )
    val addExpenseState = _actionAddExpense.uiState

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            _actionAddExpense.doAction(expense)
        }
    }

    // edit expense
    private val _actionEditExpense = UIAction<Expense, Expense?>(
        action = { repo.editExpense(it) }
    )
    val editExpenseState = _actionEditExpense.uiState

    fun editExpense(expense: Expense) {
        viewModelScope.launch {
            _actionEditExpense.doAction(expense)
        }
    }

    // delete expense
    private val _actionRemoveExpense = UIAction<Expense, Unit>(
        action = { repo.removeExpense(it) }
    )
    val removeExpenseState = _actionRemoveExpense.uiState

    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            _actionRemoveExpense.doAction(expense)
        }
    }

    // expense dialog state
    val expenseDialogStateManager = ExpenseDialogStateManager()

    val expenseDialogState get() = expenseDialogStateManager.expenseDialogStateFlow
}