package rahulstech.android.budgetapp.repository

import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

interface BudgetRepository {

    suspend fun createBudget(budget: Budget): Budget

    fun observeBudgetById(id: String): Flow<Budget?>

    fun observeAllBudgets(): Flow<List<Budget>>

    suspend fun editBudget(budget: Budget): Budget?

    suspend fun removeBudget(budget: Budget)

    suspend fun addCategory(category: BudgetCategory): BudgetCategory

    fun observeBudgetCategoriesForBudget(budgetId: String): Flow<List<BudgetCategory>>

    suspend fun editCategory(category: BudgetCategory): BudgetCategory?

    suspend fun removeCategory(category: BudgetCategory)

    suspend fun addExpense(expense: Expense): Expense

    fun observeExpensesForCategory(categoryId: String): Flow<List<Expense>>

    suspend fun editExpense(expense: Expense): Expense?

    suspend fun removeExpense(expense: Expense)
}