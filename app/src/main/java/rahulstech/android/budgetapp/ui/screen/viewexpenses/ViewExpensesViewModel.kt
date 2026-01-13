package rahulstech.android.budgetapp.ui.screen.viewexpenses

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.ExpenseFilterParams
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.ExpenseDialogState
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIText
import javax.inject.Inject

private const val TAG = "ViewExpensesViewModel"

@HiltViewModel
class ViewExpensesViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {

    private val _state = MutableStateFlow(ViewExpenseUIState())

    val state = _state.asStateFlow()

    val currentStateValue: ViewExpenseUIState get() = _state.value

    private val _effect = Channel<UISideEffect>(Channel.BUFFERED)

    val effect: Flow<UISideEffect> = _effect.receiveAsFlow()

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
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                updateAddExpenseDialog(true, expense)
                repo.addExpense(expense)
                hideAddExpenseDialog()
                _effect.send(UISideEffect.ShowSnackBar(
                    UIText.StringResource(R.string.message_expense_save_successful))
                )
            }
            catch (cause: Throwable) {
                Log.e(TAG, "remove expense error", cause)
                _effect.send(UISideEffect.ShowSnackBar(
                    UIText.StringResource(R.string.message_expense_save_error))
                )
            }
        }
    }

    // edit expense
    fun editExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                updateEditExpenseDialog(true, expense)
                repo.editExpense(expense)
                hideEditExpenseDialog()
                _effect.send(UISideEffect.ShowSnackBar(
                    UIText.StringResource(R.string.message_expense_save_successful))
                )
            }
            catch (cause: Throwable) {
                Log.e(TAG, "remove expense error", cause)
                _effect.send(UISideEffect.ShowSnackBar(
                    UIText.StringResource(R.string.message_expense_save_error))
                )
            }
        }
    }

    // remove expense
    fun removeExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repo.removeExpense(expense)
            }
            catch (cause: Throwable) {
                Log.e(TAG, "remove expense error", cause)
                _effect.send(UISideEffect.ShowSnackBar(
                    UIText.StringResource(R.string.message_expense_remove_error))
                )
            }
        }
    }

    // ui event handler

    fun onUIEvent(event: ViewExpenseUIEvent) {
        when(event) {
            is ViewExpenseUIEvent.ShowExpenseOptionsDialogEvent -> { showExpenseOptionsDialog(event.expense) }
            is ViewExpenseUIEvent.ShowEditExpenseDialogEvent -> { showEditExpenseDialog(event.expense) }
            is ViewExpenseUIEvent.ShowDeleteExpenseDialogEvent -> { showDeleteExpenseDialog(event.expense) }
        }
    }


    fun showExpenseOptionsDialog(expense: Expense) {
        _state.value = currentStateValue.copy(expenseOptionsDialog = ExpenseDialogState(showDialog = true, expense = expense))
    }

    fun hideExpenseOptionsDialog() {
        _state.value = currentStateValue.copy(expenseOptionsDialog = ExpenseDialogState())
    }

    fun showAddExpenseDialog(budgetId: Long) {
        _state.value = currentStateValue.copy(addExpenseDialog = ExpenseDialogState(showDialog = true, budgetId = budgetId))
    }

    fun hideAddExpenseDialog() {
        _state.value = currentStateValue.copy(addExpenseDialog = ExpenseDialogState())
    }

    fun updateAddExpenseDialog(isSaving: Boolean, expense: Expense = currentStateValue.addExpenseDialog.expense) {
        val current = currentStateValue.addExpenseDialog
        _state.value = currentStateValue.copy(addExpenseDialog = current.copy(isSaving = isSaving, expense = expense))
    }

    fun showEditExpenseDialog(expense: Expense) {
        _state.value = currentStateValue.copy(editExpenseDialog = ExpenseDialogState(showDialog = true, expense = expense))
    }

    fun hideEditExpenseDialog() {
        _state.value = currentStateValue.copy(editExpenseDialog = ExpenseDialogState())
    }

    fun updateEditExpenseDialog(isSaving: Boolean, expense: Expense = currentStateValue.addExpenseDialog.expense) {
        val current = currentStateValue.editExpenseDialog
        _state.value = currentStateValue.copy(editExpenseDialog = current.copy(isSaving = isSaving, expense = expense))
    }

    fun showDeleteExpenseDialog(expense: Expense) {
        _state.value = currentStateValue.copy(deleteExpenseDialog = ExpenseDialogState(showDialog = true, expense = expense))
    }

    fun hideDeleteExpenseDialog() {
        _state.value = currentStateValue.copy(deleteExpenseDialog = ExpenseDialogState())
    }
}