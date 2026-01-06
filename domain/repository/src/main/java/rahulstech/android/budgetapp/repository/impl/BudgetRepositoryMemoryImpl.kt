package rahulstech.android.budgetapp.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import java.util.UUID

internal class BudgetRepositoryMemoryImpl: BudgetRepository {

    private val budgets = mutableMapOf(
        "1" to Budget(
            id = "1",
            name = "Budget 1",
            details = "This is the details of Budget 1",
            totalExpense = 12000.0,
            totalAllocation = 20000.0,
            categories = (1..10).map { item ->
                BudgetCategory(
                    id = "$item",
                    budgetId = "1",
                    name = "Category 1.${item}",
                    note = "The is a short note of Category 1.${item}",
                    allocation = (1000..10000).random().toDouble(),
                    totalExpense = (1000..10000).random().toDouble()
                )
            }
        )
    )

    private val budgetsState = MutableStateFlow<List<Budget>>(emptyList())

    private fun updateBudgetsState() {
        budgetsState.value = budgets.values.toList()
    }


    init {
        updateBudgetsState()
    }

    override suspend fun createBudget(budget: Budget): Budget = withContext(Dispatchers.IO) {
        val totalAllocation = budget.categories.sumOf { it.allocation }
        val categories = budget.categories.map { it.copy(id = UUID.randomUUID().toString()) }
        val copy = budget.copy(id = UUID.randomUUID().toString(),
            totalAllocation = totalAllocation,
            categories = categories
        )
        budgets[copy.id] = copy
        updateBudgetsState()
        copy
    }

    override fun observeBudgetById(id: String): Flow<Budget?> = flowOf(budgets[id])

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