package rahulstech.android.budgetapp.ui.screen.createbudet

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.screen.BUDGET_CATEGORY_PLACEHOLDER

sealed interface CreateBudgetUIEvent {
    data class AddBudgetEvent(
        val budget: Budget
    ): CreateBudgetUIEvent

    data class ShowCategoryDialogEvent(
        val category: BudgetCategory = BUDGET_CATEGORY_PLACEHOLDER,
        val isEditMode: Boolean = false,
    ): CreateBudgetUIEvent

    data class SaveCategoryEvent(
        val category: BudgetCategory,
    ):  CreateBudgetUIEvent

    data class RemoveCategoryEvent(
        val category: BudgetCategory
    ):  CreateBudgetUIEvent

    data class BudgetUpdateEvent(
        val budget: Budget,
    ): CreateBudgetUIEvent
}

typealias CreateBudgetUIEventCallback = (CreateBudgetUIEvent)-> Unit

