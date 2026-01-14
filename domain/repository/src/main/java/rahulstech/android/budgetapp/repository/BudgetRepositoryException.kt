package rahulstech.android.budgetapp.repository

import java.lang.Exception

class BudgetRepositoryException(message: String, cause: Throwable? = null): Exception(message,cause) {

    companion object {
        fun budgetNotFound(budgetId: Long): BudgetRepositoryException =
            BudgetRepositoryException("budget with id=$budgetId does not exist")

        fun categoryNotFound(categoryId: Long): BudgetRepositoryException =
            BudgetRepositoryException("budget_category with id = $categoryId does not exist")

        fun expenseNotFound(expenseId: Long): BudgetRepositoryException =
            BudgetRepositoryException("expense with id = $expenseId does not exist")
    }
}