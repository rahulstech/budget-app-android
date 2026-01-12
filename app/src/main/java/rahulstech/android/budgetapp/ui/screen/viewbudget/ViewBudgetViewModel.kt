package rahulstech.android.budgetapp.ui.screen.viewbudget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.components.BudgetDialogState
import rahulstech.android.budgetapp.ui.components.ExpenseDialogState
import rahulstech.android.budgetapp.ui.screen.BUDGET_CATEGORY_PLACEHOLDER
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.Screen
import rahulstech.android.budgetapp.ui.screen.ScreenArgs
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIState
import rahulstech.android.budgetapp.ui.screen.UIText
import javax.inject.Inject

private const val TAG = "ViewBudgetViewModel"

@HiltViewModel
class ViewBudgetViewModel @Inject constructor(val repo: BudgetRepository): ViewModel()
{
    // ui states

    private val _state = MutableStateFlow(ViewBudgetUIState())

    val state = _state.asStateFlow()

    val currentStateValue: ViewBudgetUIState get() = _state.value

    private val _effect = Channel<UISideEffect>(Channel.BUFFERED)

    val effect = _effect.receiveAsFlow()

    // this is a simple guard to trigger multiple observeBudgetById for same budgetId
    private var budgetId: Long = 0

    // categories loading is triggered in budget flow collector
    // and collector may be called multiple time for each table update
    // but everytime i don't need a fresh categories reload for the same budgetId
    // this guard ensures categories load start once per budgetId not each budget refresh
    private var observeCategoriesStarted: Boolean = false

    fun observeBudget(id: Long) {
        if (budgetId == id) return
        budgetId = id
        Log.i(TAG, "observeBudget called")
        viewModelScope.launch {
            repo.observeBudgetById(id)
                .onStart {
                    updateBudgetAndCategories(UIState.Loading(), UIState.Loading())
                    observeCategoriesStarted = false
                }
                .catch {
                    updateBudgetAndCategories(UIState.Error(it), UIState.Idle())
                }
                .collectLatest { budget ->
                    Log.i(TAG, "budget refreshed")
                    if (null == budget) {
                        updateBudgetAndCategories(UIState.NotFound(), UIState.NotFound())
                    }
                    else {
                        updateBudgetAndCategories(UIState.Success(budget))
                        if (!observeCategoriesStarted) {
                            observeCategories(id)
                        }
                    }
                }
        }
    }

    private fun observeCategories(budgetId: Long) {
        viewModelScope.launch {
            repo.observeBudgetCategoriesForBudget(budgetId)
                .onStart {
                    updateBudgetAndCategories(categoryState = UIState.Loading())
                    observeCategoriesStarted = true
                }
                .catch {
                    updateBudgetAndCategories(categoryState = UIState.Error(it))
                }
                .collectLatest { categories ->
                    Log.i(TAG, "categories refreshed")
                    if (categories.isEmpty()) {
                        updateBudgetAndCategories(categoryState = UIState.NotFound())
                    }
                    else {
                        updateBudgetAndCategories(categoryState = UIState.Success(categories))
                    }
                }
        }
    }

    // remove budget
    fun removeBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                repo.removeBudget(budget)
                _effect.send(UISideEffect.ExitScreen)
            }
            catch (cause: Throwable) {
                Log.e(TAG,"remove budget error", cause)
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_remove_error)))
            }
        }
    }

    // edit budget
    fun editBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                repo.editBudget(budget)
                hideEditBudgetDialog()
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_save_success)))
            }
            catch (cause: Throwable) {
                Log.e(TAG,"edit budget error", cause)
                updateEditBudgetDialog(isSaving = false)
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_save_error)))
            }
        }
    }

    // add category
    fun addCategory(category: BudgetCategory) {
        viewModelScope.launch {
            try {
                updateAddCategoryDialog( false,category)
                repo.addCategory(category)
                hideAddCategoryDialog()
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_category_save_success)))
            }
            catch (cause: Throwable) {
                Log.e(TAG, "add category error", cause)
                updateAddCategoryDialog( false)
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_category_save_error)))
            }
        }
    }

    // edit category
    fun editCategory(category: BudgetCategory) {
        Log.i(TAG,"edit category = $category")
        viewModelScope.launch {
            try {
                updateEditCategoryDialog(false,category)
                repo.editCategory(category)
                hideEditCategoryDialog()
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_category_save_success)))
            }
            catch (cause: Throwable) {
                Log.e(TAG, "edit category error", cause)
                updateEditCategoryDialog(false)
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_category_save_error)))
            }
        }
    }

    // delete category
    fun removeCategory(category: BudgetCategory) {
        viewModelScope.launch {
            try {
                repo.removeCategory(category)
                hideDeleteCategoryDialog()
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_category_remove_success)))
            }
            catch (cause: Throwable) {
                Log.e(TAG, "delete category error", cause)
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_budget_category_remove_error)))
            }
        }
    }

    // add expense
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                updateAddExpenseDialog(true, expense)
                repo.addExpense(expense)
                hideAddExpenseDialog()
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_expense_add_successful)))
            }
            catch (cause: Throwable) {
                Log.e(TAG, "add expense error", cause)
                updateAddExpenseDialog(isSaving = false)
                _effect.send(UISideEffect.ShowSnackBar(UIText.StringResource(R.string.message_expense_add_error)))
            }
        }
    }

    // ui event handler
    fun onUIEvent(event: ViewBudgetUIEvent) {
        when(event) {
            is ViewBudgetUIEvent.ShowEditBudgetDialogEvent -> {
                showEditBudgetDialog(event.budget)
            }
            is ViewBudgetUIEvent.EditBudgetEvent -> {
                editBudget(event.budget)
            }

            is ViewBudgetUIEvent.ShowAddCategoryDialogEvent -> {
                showAddCategoryDialog(event.budget)
            }
            is ViewBudgetUIEvent.AddCategoryEvent -> {
                addCategory(event.category)
            }

            is ViewBudgetUIEvent.ShowAddExpenseDialogEvent -> {
                showAddExpenseDialog(budgetId, event.category)
            }
            is ViewBudgetUIEvent.AddExpenseEvent -> { addExpense(event.expense) }

            is ViewBudgetUIEvent.ViewExpensesEvent -> {
                viewModelScope.launch {
                    _effect.send(UISideEffect.NavigateTo(NavigationEvent.ForwardTo(Screen.ViewExpenses,ScreenArgs(budgetId = event.budgetId, categoryId = event.categoryId))))
                }
            }

            is ViewBudgetUIEvent.DeleteBudgetEvent -> {
                showDeleteBudgetDialog(event.budget)
            }

            is ViewBudgetUIEvent.ShowCategoryOptionsEvent -> {
                showCategoryOptionsDialog(event.category)
            }

            is ViewBudgetUIEvent.ShowBudgetDetailsEvent -> {
                showBudgetDetailsDialog(event.budget)
            }

            is ViewBudgetUIEvent.ShowEditCategoryDialogEvent -> {
                showEditCategoryDialog(event.category)
                hideCategoryOptionsDialog()
            }

            is ViewBudgetUIEvent.ShowDeleteCategoryDialogEvent -> {
                hideCategoryOptionsDialog()
                showDeleteCategoryDialog(event.category)
            }
        }
    }

    fun updateBudgetAndCategories(budgetState: UIState<Budget> = currentStateValue.budgetState,
                                  categoryState: UIState<List<BudgetCategory>> = currentStateValue.categoryState) {
        _state.value = _state.value.copy(budgetState = budgetState, categoryState = categoryState)
    }

    fun showAddCategoryDialog(budget: Budget) {
        _state.value = _state.value.copy(addCategoryDialog = BudgetCategoryDialogState(showDialog = true, budget = budget))
    }

    fun hideAddCategoryDialog() {
        _state.value = _state.value.copy(addCategoryDialog = BudgetCategoryDialogState())
    }

    fun updateAddCategoryDialog(isSaving: Boolean, category: BudgetCategory = currentStateValue.addCategoryDialog.category) {
        val current = currentStateValue.addCategoryDialog
        _state.value = _state.value.copy(addCategoryDialog = current.copy(isSaving = isSaving, category = category))
    }

    fun showAddExpenseDialog(budgetId: Long, category: BudgetCategory = BUDGET_CATEGORY_PLACEHOLDER) {
        val dialogState = ExpenseDialogState(showDialog = true, budgetId = budgetId, category = category )
        _state.value = _state.value.copy(addExpenseDialog = dialogState)
    }

    fun hideAddExpenseDialog() {
        _state.value = _state.value.copy(addExpenseDialog = ExpenseDialogState())
    }

    fun updateAddExpenseDialog(isSaving: Boolean, expense: Expense = currentStateValue.addExpenseDialog.expense) {
        val current = _state.value.addExpenseDialog
        _state.value = _state.value.copy(addExpenseDialog = current.copy(isSaving = isSaving, expense = expense))
    }

    fun showEditBudgetDialog(budget: Budget) {
        _state.value = _state.value.copy(editBudgetDialog = BudgetDialogState(showDialog = true, budget = budget))
    }

    fun hideEditBudgetDialog() {
        _state.value = _state.value.copy(editBudgetDialog = BudgetDialogState())
    }

    fun updateEditBudgetDialog(isSaving: Boolean, budget: Budget = currentStateValue.editBudgetDialog.budget) {
        val current = currentStateValue.editBudgetDialog
        _state.value = _state.value.copy(editBudgetDialog = current.copy(isSaving = isSaving, budget = budget))
    }

    fun showDeleteBudgetDialog(budget: Budget) {
        _state.value = _state.value.copy(deleteBudgetDialog = BudgetDialogState(showDialog = true, budget = budget))
    }

    fun hideDeleteBudgetDialog() {
        _state.value = _state.value.copy(deleteBudgetDialog = BudgetDialogState())
    }

    fun showBudgetDetailsDialog(budget: Budget) {
        _state.value = _state.value.copy(budgetDetailsDialog = BudgetDialogState(showDialog = true, budget = budget))
    }

    fun hideBudgetDetailsDialog() {
        _state.value = _state.value.copy(budgetDetailsDialog = BudgetDialogState())
    }

    fun showCategoryOptionsDialog(category: BudgetCategory) {
        _state.value = _state.value.copy(categoryOptionsDialog = BudgetCategoryDialogState(showDialog = true, category = category))
    }

    fun hideCategoryOptionsDialog() {
        _state.value = _state.value.copy(categoryOptionsDialog = BudgetCategoryDialogState())
    }

    fun showEditCategoryDialog(category: BudgetCategory) {
        _state.value = _state.value.copy(editCategoryDialog = BudgetCategoryDialogState(showDialog = true, category = category))
    }

    fun updateEditCategoryDialog(isSaving: Boolean, category: BudgetCategory = currentStateValue.editCategoryDialog.category) {
        val current = currentStateValue.editCategoryDialog
        _state.value = _state.value.copy(editCategoryDialog = current.copy(isSaving = isSaving, category = category))
    }

    fun hideEditCategoryDialog() {
        _state.value = _state.value.copy(editCategoryDialog = BudgetCategoryDialogState())
    }

    fun showDeleteCategoryDialog(category: BudgetCategory) {
        _state.value = _state.value.copy(deleteCategoryDialog = BudgetCategoryDialogState(showDialog = true, category = category))
    }

    fun hideDeleteCategoryDialog() {
        _state.value = _state.value.copy(deleteCategoryDialog = BudgetCategoryDialogState())
    }
}
