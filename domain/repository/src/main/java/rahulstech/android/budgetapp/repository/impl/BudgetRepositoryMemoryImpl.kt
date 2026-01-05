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
            totalAllocation = 16500.0,
            totalExpense = 11900.0,
            categories = listOf(
                BudgetCategory(
                    id = "1",
                    budgetId = "1",
                    name = "Category 1.1",
                    note = "The is a short note of Category 1.1",
                    allocation = 2500.0,
                    totalExpense = 1100.0
                ),

                BudgetCategory(
                    id = "2",
                    budgetId = "1",
                    name = "Category 1.2",
                    note = "The is a short note of Category 1.2",
                    allocation = 5000.0,
                    totalExpense = 3800.0
                ),

                BudgetCategory(
                    id = "3",
                    budgetId = "1",
                    name = "Category 1.1",
                    note = "The is a short note of Category 1.1",
                    allocation = 9000.0,
                    totalExpense = 7000.0
                ),
            )
        )
    )

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


    init {
        updateBudgetsState()
    }

    override suspend fun createBudget(budget: Budget): Budget = withContext(Dispatchers.IO) {
        val totalAllocation = budget.categories.sumOf { it.allocation }
        val copy = budget.copy(id = UUID.randomUUID().toString(), totalAllocation = totalAllocation)
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