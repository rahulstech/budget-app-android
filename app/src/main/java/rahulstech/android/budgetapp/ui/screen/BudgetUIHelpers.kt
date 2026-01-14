package rahulstech.android.budgetapp.ui.screen

import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

val BUDGET_PLACEHOLDER = Budget(name = "")

val BUDGET_CATEGORY_PLACEHOLDER = BudgetCategory(budgetId = 0, name = "")

val EXPENSE_PLACEHOLDER = Expense(budgetId = 0, categoryId = 0)