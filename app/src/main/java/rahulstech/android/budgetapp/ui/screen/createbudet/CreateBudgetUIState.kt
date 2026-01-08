package rahulstech.android.budgetapp.ui.screen.createbudet

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory

data class CreateBudgetUIState(
    val budget: Budget = Budget(name = "", details = ""),
    val categories: List<BudgetCategory> = listOf(),
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = budget.name.isNotBlank() && categories.isNotEmpty()
}
