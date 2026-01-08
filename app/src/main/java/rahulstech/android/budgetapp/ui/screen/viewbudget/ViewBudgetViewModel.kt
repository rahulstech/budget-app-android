package rahulstech.android.budgetapp.ui.screen.viewbudget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.components.BudgetDialogState
import rahulstech.android.budgetapp.ui.components.ExpenseDialogState
import rahulstech.android.budgetapp.ui.screen.UIAction
import rahulstech.android.budgetapp.ui.screen.UIState
import javax.inject.Inject

private const val TAG = "ViewBudgetViewModel"

@HiltViewModel
class ViewBudgetViewModel @Inject constructor(
    val repo: BudgetRepository
): ViewModel()
{

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
    private val actionEditBudget = UIAction<Budget, Budget>(
        action = { repo.editBudget(it) },
        converter = { arg, result ->
            if (null == result) {
                UIState.NotFound()
            } else {
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
    private val actionAddExpense = UIAction<Expense, Expense>(
        action = { repo.addExpense(it) }
    )
    val expenseSaveState = actionAddExpense.uiState

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            actionAddExpense.doAction(expense)
        }
    }

    // ui states

    private val _budgetEditDialogState = MutableStateFlow(BudgetDialogState())
    val budgetEditDialogState: StateFlow<BudgetDialogState> = _budgetEditDialogState.asStateFlow()

    fun updateBudgetEditDialogState(state: BudgetDialogState) {
        _budgetEditDialogState.value = state
    }

    private val _categoryDialogState = MutableStateFlow(BudgetCategoryDialogState())
    val categoryDialogState: StateFlow<BudgetCategoryDialogState> = _categoryDialogState.asStateFlow()

    fun updateCategoryDialogState(state: BudgetCategoryDialogState) {
        _categoryDialogState.value = state
    }
    private val _expenseDialogState = MutableStateFlow(ExpenseDialogState())
    val expenseDialogState: StateFlow<ExpenseDialogState> = _expenseDialogState.asStateFlow()

    fun updateExpenseDialogState(state: ExpenseDialogState) {
        _expenseDialogState.value = state
    }
}
