package rahulstech.android.budgetapp.ui.screen.viewbudget

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.screen.UIState

sealed interface ViewBudgetUIEvent {

    data class EditBudgetEvent(
        val budget: Budget
    ): ViewBudgetUIEvent

    data class AddCategoryEvent(
        val category: BudgetCategory
    ): ViewBudgetUIEvent

    data class AddExpenseEvent(
        val expense: Expense
    ): ViewBudgetUIEvent

    data class ShowAddExpenseDialogEvent(
        val category: BudgetCategory
    ): ViewBudgetUIEvent

    data class ShowAddCategoryDialogEvent(
        val budget: Budget
    ): ViewBudgetUIEvent

    data class ShowEditBudgetDialogEvent(
        val budget: Budget
    ): ViewBudgetUIEvent

    data class ViewExpenses(
        val budgetId: Long,
        val categoryId: Long? = null
    ): ViewBudgetUIEvent
}

typealias ViewBudgetUIEventCallback = (ViewBudgetUIEvent)-> Unit

data class ViewBudgetUIState(
    val budgetState: UIState<Budget> = UIState.Idle(),
    val categoryState: UIState<List<BudgetCategory>> = UIState.Idle(),
) {
    companion object {
        fun loadingBoth(): ViewBudgetUIState =
            ViewBudgetUIState(UIState.Loading(), UIState.Loading())

        fun budgetError(cause: Throwable?): ViewBudgetUIState =
            ViewBudgetUIState(budgetState = UIState.Error(cause), categoryState = UIState.Idle())

        fun budgetNotFound(): ViewBudgetUIState =
            ViewBudgetUIState(budgetState = UIState.NotFound(), categoryState = UIState.Idle())
    }

    fun budgetLoaded(budget: Budget): ViewBudgetUIState =
        copy(budgetState = UIState.Success(budget))

    fun categoriesLoaded(categories: List<BudgetCategory>): ViewBudgetUIState =
        copy(categoryState = UIState.Success(categories))

    fun categoriesError(cause: Throwable?): ViewBudgetUIState =
        copy(categoryState = UIState.Error(cause))
}