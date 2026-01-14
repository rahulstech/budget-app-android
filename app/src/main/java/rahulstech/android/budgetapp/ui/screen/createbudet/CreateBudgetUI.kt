package rahulstech.android.budgetapp.ui.screen.createbudet

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.screen.BUDGET_CATEGORY_PLACEHOLDER
import rahulstech.android.budgetapp.ui.screen.BUDGET_PLACEHOLDER

sealed interface CreateBudgetUIEvent {

    data class ShowCategoryDialogEvent(val category: BudgetCategory = BUDGET_CATEGORY_PLACEHOLDER): CreateBudgetUIEvent

    data class RemoveCategoryEvent(
        val category: BudgetCategory
    ):  CreateBudgetUIEvent

    data class BudgetUpdateEvent(
        val budget: Budget,
    ): CreateBudgetUIEvent
}

typealias CreateBudgetUIEventCallback = (CreateBudgetUIEvent)-> Unit

data class CreateBudgetUIState(
    val budget: Budget = BUDGET_PLACEHOLDER,
    val categories: List<BudgetCategory> = listOf(),
    val categoryDialog: BudgetCategoryDialogState = BudgetCategoryDialogState(),
    val isSaving: Boolean = false,
) {
    val canSave: Boolean get() = !isSaving && budget.name.isNotBlank() && categories.isNotEmpty()

    fun prepareBudget(): Budget = budget.copy(categories = categories)
}