package rahulstech.android.budgetapp.ui.screen.createbudet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.screen.BUDGET_CATEGORY_PLACEHOLDER
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.Screen
import rahulstech.android.budgetapp.ui.screen.ScreenArgs
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIText
import javax.inject.Inject

private const val TAG = "CreateBudgetViewModel"

@HiltViewModel
class CreateBudgetViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {

    private val _state = MutableStateFlow(CreateBudgetUIState())

    val state: StateFlow<CreateBudgetUIState> = _state.asStateFlow()

    private val _effect = Channel<UISideEffect>(Channel.BUFFERED)

    val effect: Flow<UISideEffect> = _effect.receiveAsFlow()

    fun createBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                updateSaving(true)

                val newBudget = repo.createBudget(budget)

                sendEffect(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_save_success)))

                sendEffect(UISideEffect.ExitScreen)

                sendEffect(UISideEffect.NavigateTo(
                    NavigationEvent.ForwardTo(Screen.ViewBudget,
                        ScreenArgs(budgetId = newBudget.id)))
                )
            } catch (cause: Throwable) {
                Log.e(TAG, "create budget error",cause)
                updateSaving(false)
                sendEffect(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_save_error)))
            }
        }
    }

    private fun updateSaving(isSaving: Boolean) {
        _state.value = _state.value.copy(isSaving = isSaving)
    }

    private suspend fun sendEffect(effect: UISideEffect) {
        _effect.send(effect)
    }

    private var lastCategoryId: Long = 0

    fun onUIEvent(event: CreateBudgetUIEvent) {
        when (event) {
            is CreateBudgetUIEvent.ShowCategoryDialogEvent -> {
                showCategoryDialog(event.category)
            }
            is CreateBudgetUIEvent.RemoveCategoryEvent -> {
                removeCategory(event.category)
            }
            is CreateBudgetUIEvent.BudgetUpdateEvent -> {
                updateBudget(event.budget)
            }
        }
    }

    fun updateBudget(budget: Budget) {
        _state.value = _state.value.copy(budget = budget)
    }

    fun saveCategory(category: BudgetCategory) {
        val currentState = _state.value
        val categories = currentState.categories
        if (category.id == 0L) {
            // save new
            _state.value = currentState.copy(
                categories = categories + category.copy(id = ++lastCategoryId)
            )
        }
        else {
            // edit
            _state.value = currentState.copy(
                categories = categories.map {
                    if (it.id == category.id) category.copy() else it
                }
            )
        }
    }

    fun removeCategory(category: BudgetCategory) {
        val currentState = _state.value
        val categories = currentState.categories
        _state.value = currentState.copy(
            categories = categories.filter { it.id != category.id }
        )
    }

    fun showCategoryDialog(category: BudgetCategory = BUDGET_CATEGORY_PLACEHOLDER) {
        _state.value = _state.value.copy(
            categoryDialog = BudgetCategoryDialogState(showDialog = true, category = category)
        )
    }

    fun hideCategoryDialog() {
        _state.value = _state.value.copy(categoryDialog = BudgetCategoryDialogState())
    }

}