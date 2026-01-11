package rahulstech.android.budgetapp.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

interface BudgetRepository {

    suspend fun createBudget(budget: Budget): Budget

    fun observeBudgetById(id: Long): Flow<Budget?>

    fun observeAllBudgets(): Flow<PagingData<Budget>>

    suspend fun editBudget(budget: Budget): Budget?

    suspend fun removeBudget(budget: Budget)

    suspend fun addCategory(category: BudgetCategory): BudgetCategory

    fun observeBudgetCategoriesForBudget(budgetId: Long): Flow<List<BudgetCategory>>

    suspend fun editCategory(category: BudgetCategory): BudgetCategory?

    suspend fun removeCategory(category: BudgetCategory, reverseAmounts: Boolean = true)

    suspend fun addExpense(expense: Expense): Expense

    suspend fun observeExpenses(params: ExpenseFilterParams): Flow<PagingData<Expense>>

    suspend fun editExpense(expense: Expense): Expense?

    suspend fun removeExpense(expense: Expense, reverseAmounts: Boolean = true)
}