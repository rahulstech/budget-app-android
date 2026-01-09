package rahulstech.android.budgetapp.repository.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

internal class BudgetRepositoryMemoryImpl: BudgetRepository {

    private val budgets = mutableMapOf(
        1L to Budget(
            id = 1L,
            name = "Budget 1",
            details = "This is the details of Budget 1",
            totalExpense = 12000.0,
            totalAllocation = 20000.0,
            categories = (1..10).map { item ->
                BudgetCategory(
                    id = item.toLong(),
                    budgetId = 1L,
                    name = "Category 1.${item}",
                    note = "The is a short note of Category 1.${item}",
                    allocation = (1000..10000).random().toDouble(),
                    totalExpense = (1000..10000).random().toDouble()
                )
            }
        )
    )

    private val budgetsState = MutableStateFlow<List<Budget>>(emptyList())

    private var budgetId: Long = 0
    private val budgetState = MutableStateFlow<Budget?>(null)

    private var categoryBudgetId: Long = 0

    private val categoriesState = MutableStateFlow<List<BudgetCategory>>(emptyList())

    private var lastBudgetId: Long = 0

    private var lastCategoryId: Long = 0

    private var lastExpenseId: Long = 0

    private fun updateBudgetsState() {
        Log.i("BudgetRepositoryMemoryImpl", "updateBudgetsState")
        budgetsState.value = budgets.values.toList()
        updateBudgetState()
    }

    private fun updateBudgetState() {
        budgetState.value = budgets[budgetId]
    }

    private fun updateCategoriesState() {
        categoriesState.value = budgets[categoryBudgetId]?.categories ?: emptyList()
    }

    init {
        updateBudgetsState()
    }

    override suspend fun createBudget(budget: Budget): Budget = withContext(Dispatchers.IO) {
        val totalAllocation = budget.categories.sumOf { it.allocation }
        val categories = budget.categories.map { it.copy(id = lastBudgetId++) }
        val copy = budget.copy(id = lastCategoryId++,
            totalAllocation = totalAllocation,
            categories = categories
        )
        budgets[copy.id] = copy
        updateBudgetsState()
        copy
    }

    override fun observeBudgetById(id: Long): Flow<Budget?> {
        budgetId = id
        updateBudgetState()
        return budgetState
    }

    override fun observeAllBudgets(): Flow<List<Budget>> = budgetsState

    override suspend fun editBudget(budget: Budget): Budget? = withContext(Dispatchers.IO) {
        budgets[budget.id]?.let { original ->
            val copy = original.copy(
                name = budget.name,
                details = budget.details
            )
            budgets[budget.id] = copy
            updateBudgetsState()
            copy
        }
    }

    override suspend fun removeBudget(budget: Budget) {}

    override suspend fun addCategory(category: BudgetCategory): BudgetCategory = withContext(Dispatchers.IO) {
        budgets[category.budgetId]?.let { budget ->
            val copy = category.copy(id = lastCategoryId++)
            budgets[budget.id] = budget.copy(
                totalAllocation = budget.totalAllocation + copy.allocation,
                categories = budget.categories + copy
            )
            updateBudgetsState()
            copy
        } ?: throw Exception()
    }

    override fun observeBudgetCategoriesForBudget(budgetId: Long): Flow<List<BudgetCategory>> {
        categoryBudgetId = budgetId
        updateCategoriesState()
        return categoriesState
    }

    override suspend fun editCategory(category: BudgetCategory): BudgetCategory? = category

    override suspend fun removeCategory(category: BudgetCategory) {}

    override suspend fun addExpense(expense: Expense): Expense = withContext(Dispatchers.IO) {
        val budgetId = expense.budgetId
        val categoryId = expense.categoryId
        // get budget and category; if not found then nothing to save
        budgets[budgetId]?.let { budget ->
            val categories = budget.categories.toMutableList()
            val categoryIndex = categories.indexOfFirst { it.id == categoryId }
            if (categoryIndex < 0) return@let null
            val copy = expense.copy(id = lastExpenseId++)
            val category = categories[categoryIndex]
            categories[categoryIndex] = category.copy(totalExpense = category.totalExpense + expense.amount)
            budgets[budgetId] = budget.copy(
                totalExpense = budget.totalExpense + expense.amount,
                categories = categories.toList()
            )
            updateBudgetsState()
            copy
        } ?: throw Exception()
    }

    override fun observeExpensesForCategory(categoryId: Long): Flow<List<Expense>> = flowOf(emptyList())

    override suspend fun editExpense(expense: Expense): Expense? = expense.copy()

    override suspend fun removeExpense(expense: Expense) {}
}