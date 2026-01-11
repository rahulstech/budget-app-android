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
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.ExpenseFilterParams
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
}