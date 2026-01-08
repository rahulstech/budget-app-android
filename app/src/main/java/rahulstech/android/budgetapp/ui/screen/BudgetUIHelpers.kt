package rahulstech.android.budgetapp.ui.screen

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

val BUDGET_PLACEHOLDER get() = Budget(name = "")

val BUDGET_CATEGORY_PLACEHOLDER get() = BudgetCategory(budgetId = "", name = "")

val EXPENSE_PLACEHOLDER get() = Expense(budgetId = "", categoryId = "")