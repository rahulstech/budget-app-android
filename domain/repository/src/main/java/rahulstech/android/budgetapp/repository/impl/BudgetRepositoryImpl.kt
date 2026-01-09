package rahulstech.android.budgetapp.repository.impl

import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.budgetdb.IBudgetDB
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

class BudgetRepositoryImpl(val db: IBudgetDB): BudgetRepository {

    override suspend fun createBudget(budget: Budget): Budget {
        TODO("Not yet implemented")
    }

    override fun observeBudgetById(id: Long): Flow<Budget?> {
        TODO("Not yet implemented")
    }

    override fun observeAllBudgets(): Flow<List<Budget>> {
        TODO("Not yet implemented")
    }

    override suspend fun editBudget(budget: Budget): Budget? {
        TODO("Not yet implemented")
    }

    override suspend fun removeBudget(budget: Budget) {
        TODO("Not yet implemented")
    }

    override suspend fun addCategory(category: BudgetCategory): BudgetCategory {
        TODO("Not yet implemented")
    }

    override fun observeBudgetCategoriesForBudget(budgetId: Long): Flow<List<BudgetCategory>> {
        TODO("Not yet implemented")
    }

    override suspend fun editCategory(category: BudgetCategory): BudgetCategory? {
        TODO("Not yet implemented")
    }

    override suspend fun removeCategory(category: BudgetCategory) {
        TODO("Not yet implemented")
    }

    override suspend fun addExpense(expense: Expense): Expense {
        TODO("Not yet implemented")
    }

    override fun observeExpensesForCategory(categoryId: Long): Flow<List<Expense>> {
        TODO("Not yet implemented")
    }

    override suspend fun editExpense(expense: Expense): Expense? {
        TODO("Not yet implemented")
    }

    override suspend fun removeExpense(expense: Expense) {
        TODO("Not yet implemented")
    }
}