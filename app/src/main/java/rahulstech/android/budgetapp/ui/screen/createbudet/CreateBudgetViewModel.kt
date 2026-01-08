package rahulstech.android.budgetapp.ui.screen.createbudet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.screen.UIState
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateBudgetViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {

    private val _budgetSaveState = MutableSharedFlow<UIState<Budget>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val budgetSaveState = _budgetSaveState.asSharedFlow()

    fun createBudget(budget: Budget) {
        viewModelScope.launch {
            // set saveState loading
            _budgetSaveState.tryEmit(UIState.Loading())
            try {
                val newBudget = repo.createBudget(budget)
                _budgetSaveState.tryEmit(UIState.Success(newBudget))
            }
            catch (cause: Throwable) {
                _budgetSaveState.tryEmit(UIState.Error(cause))
            }
        }
    }

    // ui state

    private val _createBudgetUiState = MutableStateFlow(CreateBudgetUIState())
    val createBudgetUIState: StateFlow<CreateBudgetUIState> = _createBudgetUiState.asStateFlow()

    fun updateBudget(budget: Budget) {
        _createBudgetUiState.value = _createBudgetUiState.value.copy(budget = budget)
    }

    fun saveCategory(category: BudgetCategory) {
        val currentState = _createBudgetUiState.value
        val categories = currentState.categories
        if (category.id.isBlank()) {
            // save new
            _createBudgetUiState.value = currentState.copy(
                categories = categories + category.copy(id = UUID.randomUUID().toString())
            )
        }
        else {
            // edit
            _createBudgetUiState.value = currentState.copy(
                categories = categories.map {
                    if (it.id == category.id) category.copy() else it
                }
            )
        }
    }

    fun removeCategory(category: BudgetCategory) {
        val currentState = _createBudgetUiState.value
        val categories = currentState.categories
        _createBudgetUiState.value = currentState.copy(
            categories = categories.filter { it.id != category.id }
        )
    }

    private val _categoryDialogState = MutableStateFlow(BudgetCategoryDialogState())
    val categoryDialogState: StateFlow<BudgetCategoryDialogState> = _categoryDialogState.asStateFlow()

    fun updateCategoryDialogState(state: BudgetCategoryDialogState) {
        _categoryDialogState.value = state
    }
}