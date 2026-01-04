package rahulstech.android.budgetapp.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import java.util.UUID

internal class BudgetRepositoryMemoryImpl: BudgetRepository {

    private val budgets = mutableMapOf<String, Budget>()

    private val budgetsState = MutableStateFlow<List<Budget>>(emptyList())

    private fun updateBudgetsState() {
        budgetsState.value = budgets.values.map {
            Budget(
                id = it.id,
                name = it.name,
                details = it.details,
                totalAllocation = it.totalAllocation,
                totalExpense = it.totalExpense,
            )
        }
    }


    override suspend fun createBudget(budget: Budget): Budget = withContext(Dispatchers.IO) {
        val totalAllocation = budget.categories.sumOf { it.allocation }
        val copy = budget.copy(id = UUID.randomUUID().toString(), totalAllocation = totalAllocation)
        budgets[copy.id] = copy
        updateBudgetsState()
        copy
    }

    override fun observeBudgetById(id: String): Flow<Budget?> = emptyFlow()

    override fun observeAllBudgets(): Flow<List<Budget>> = budgetsState

    override suspend fun editBudget(budget: Budget): Budget? = budget.copy()

    override suspend fun removeBudget(budget: Budget) {}

    override suspend fun addCategory(category: BudgetCategory): BudgetCategory = category.copy(id = UUID.randomUUID().toString())

    override suspend fun editCategory(category: BudgetCategory): BudgetCategory? = category

    override suspend fun removeCategory(category: BudgetCategory) {
    }

    override suspend fun addExpense(expense: Expense): Expense = expense.copy(id = UUID.randomUUID().toString())

    override fun observeExpenseForCategory(categoryId: String): Flow<List<Expense>> = flowOf(emptyList())

    override suspend fun editExpense(expense: Expense): Expense? = expense.copy()

    override suspend fun removeExpense(expense: Expense) {

    }
}