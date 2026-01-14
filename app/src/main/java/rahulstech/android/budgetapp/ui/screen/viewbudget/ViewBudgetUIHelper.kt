package rahulstech.android.budgetapp.ui.screen.viewbudget

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.components.BudgetDialogState
import rahulstech.android.budgetapp.ui.components.ExpenseDialogState
import rahulstech.android.budgetapp.ui.screen.UIState

sealed interface ViewBudgetUIEvent {

    data class ShowAddExpenseDialogEvent(val category: BudgetCategory): ViewBudgetUIEvent

    data class ShowAddCategoryDialogEvent(val budget: Budget): ViewBudgetUIEvent

    data class ViewExpensesEvent(
        val budgetId: Long,
        val categoryId: Long? = null
    ): ViewBudgetUIEvent

    data class ShowCategoryOptionsEvent(val category: BudgetCategory): ViewBudgetUIEvent

    data class ShowEditCategoryDialogEvent(val category: BudgetCategory): ViewBudgetUIEvent

    data class ShowDeleteCategoryDialogEvent(val category: BudgetCategory): ViewBudgetUIEvent
}

typealias ViewBudgetUIEventCallback = (ViewBudgetUIEvent)-> Unit

data class ViewBudgetUIState(
    val budgetState: UIState<Budget> = UIState.Idle(),
    val categoryState: UIState<List<BudgetCategory>> = UIState.Idle(),
    val editBudgetDialog: BudgetDialogState = BudgetDialogState(),
    val budgetDetailsDialog: BudgetDialogState = BudgetDialogState(),
    val deleteBudgetDialog: BudgetDialogState = BudgetDialogState(),
    val addCategoryDialog: BudgetCategoryDialogState = BudgetCategoryDialogState(),
    val categoryOptionsDialog: BudgetCategoryDialogState = BudgetCategoryDialogState(),
    val editCategoryDialog: BudgetCategoryDialogState = BudgetCategoryDialogState(),
    val deleteCategoryDialog: BudgetCategoryDialogState = BudgetCategoryDialogState(),
    val addExpenseDialog: ExpenseDialogState = ExpenseDialogState(),
)

