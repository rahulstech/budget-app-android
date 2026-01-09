package rahulstech.android.budgetapp.ui.screen.viewbudget

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

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
