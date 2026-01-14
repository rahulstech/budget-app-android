package rahulstech.android.budgetapp.repository.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import rahulstech.android.budgetapp.budgetdb.IBudgetDB
import rahulstech.android.budgetapp.budgetdb.entity.BudgetCategoryEntity
import rahulstech.android.budgetapp.budgetdb.entity.BudgetEntity
import rahulstech.android.budgetapp.budgetdb.entity.ExpenseEntity
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.BudgetRepositoryException
import rahulstech.android.budgetapp.repository.ExpenseFilterParams
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.repository.toEntity
import rahulstech.android.budgetapp.repository.toBudgetCategory

class BudgetRepositoryImpl(val db: IBudgetDB): BudgetRepository {

    override suspend fun createBudget(budget: Budget): Budget =
        db.runInTransaction {

            // 1. Insert budget shell (totals start at 0)
            val budgetId = db.budgetDao.insert(
                budget.copy(
                    id = 0,
                    totalAllocation = 0.0,
                    totalExpense = 0.0
                ).toEntity()
            )

            // 2. Insert categories (if any)
            var totalAllocation = 0.0
            var totalExpense = 0.0

            val createdCategories = budget.categories.map { category ->
                val entity = category.copy(
                    id = 0,
                    budgetId = budgetId
                ).toEntity()

                val categoryId = db.budgetCategoryDao.insert(entity)

                totalAllocation += category.allocation
                totalExpense += category.totalExpense

                category.copy(
                    id = categoryId,
                    budgetId = budgetId
                )
            }

            // 3. Update derived totals on budget
            db.budgetDao.update(
                BudgetEntity(
                    id = budgetId,
                    name = budget.name,
                    details = budget.details,
                    totalAllocation = totalAllocation,
                    totalExpense = totalExpense
                )
            )

            // 4. Return fully formed domain model
            budget.copy(
                id = budgetId,
                totalAllocation = totalAllocation,
                totalExpense = totalExpense,
                categories = createdCategories
            )
        }

    override fun observeBudgetById(id: Long): Flow<Budget?> =
        db.budgetDao.observeBudgetById(id).map { it?.toBudgetCategory() }

    override fun observeAllBudgets(): Flow<PagingData<Budget>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                db.budgetDao.observerAllBudgets()
            }
        ).flow
            .map { pagingData ->
                pagingData.map { model ->
                    Budget(
                        id = model.id,
                        name = model.name,
                        totalAllocation = model.totalAllocation,
                        totalExpense = model.totalExpense
                    )
                }
            }
    }

    override suspend fun editBudget(budget: Budget): Budget?  = db.runInTransaction {
        val old = db.budgetDao
            .observeBudgetById(budget.id)
            .first()
            ?: return@runInTransaction null

        val updated = budget.copy(totalExpense = old.totalExpense, totalAllocation = old.totalAllocation)
        db.budgetDao.update(updated.toEntity())

        updated
    }

    override suspend fun removeBudget(budget: Budget) = db.runInTransaction {
        db.budgetDao.delete(budget.toEntity())
    }

    override suspend fun addCategory(category: BudgetCategory): BudgetCategory =
        db.runInTransaction {

            val budget = getBudgetEntityById(category.budgetId)
                ?: throw BudgetRepositoryException.budgetNotFound(category.budgetId)

            val id = db.budgetCategoryDao.insert(category.copy(id = 0).toEntity())

            db.budgetDao.update(
                budget.copy(
                    totalAllocation = budget.totalAllocation + category.allocation,
                    lastModified = System.currentTimeMillis()
                )
            )

            category.copy(id = id)
        }

    override fun observeBudgetCategoriesForBudget(budgetId: Long): Flow<List<BudgetCategory>> {
        return db.budgetCategoryDao
            .observeCategoriesOfBudget(budgetId)
            .map { list ->
                list.map { it.toBudgetCategory() }
            }
    }

    override suspend fun editCategory(category: BudgetCategory): BudgetCategory? =
        db.runInTransaction {
            val old = getBudgetCategoryEntityById(category.id) ?: return@runInTransaction null

            val budget = getBudgetEntityById(category.budgetId) ?: return@runInTransaction null

            val allocationDiff = category.allocation - old.allocation

            db.budgetCategoryDao.update(category.copy(totalExpense = old.totalExpense).toEntity())

            db.budgetDao.update(
                budget.copy(
                    totalAllocation = budget.totalAllocation + allocationDiff,
                    lastModified = System.currentTimeMillis()
                )
            )

            category.copy()
        }

    override suspend fun removeCategory(category: BudgetCategory, reverseAmounts: Boolean) {
        db.runInTransaction {
            val old = getBudgetCategoryEntityById(category.id) ?: return@runInTransaction
            val budget = getBudgetEntityById(category.budgetId) ?: return@runInTransaction

            db.budgetCategoryDao.delete(category.toEntity())

            if (reverseAmounts) {
                db.budgetDao.update(
                    budget.copy(
                        totalAllocation = budget.totalAllocation - old.allocation,
                        totalExpense = budget.totalExpense - old.totalExpense,
                        lastModified = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override suspend fun addExpense(expense: Expense): Expense =
        db.runInTransaction {
            val budget = getBudgetEntityById(expense.budgetId)
                ?: throw BudgetRepositoryException.budgetNotFound(expense.budgetId)

            val category = getBudgetCategoryEntityById(expense.categoryId)
                ?: throw BudgetRepositoryException.categoryNotFound(expense.categoryId)

            val id = db.expenseDao.insert(expense.toEntity())

            db.budgetCategoryDao.update(
                category.copy(
                    totalExpense = category.totalExpense + expense.amount,
                    lastModified = System.currentTimeMillis()
                )
            )

            db.budgetDao.update(
                budget.copy(
                    totalExpense = budget.totalExpense + expense.amount,
                    lastModified = System.currentTimeMillis()
                )
            )

            expense.copy(id = id)
        }

    override suspend fun observeExpenses(params: ExpenseFilterParams): Flow<PagingData<Expense>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = { params.createPageSource(db.expenseDao) }
        ).flow
            .map { pagingData ->
                pagingData.map { (expense, categoryName) ->
                    Expense(
                        id = expense.id,
                        budgetId = expense.budgetId,
                        categoryId = expense.categoryId,
                        note = expense.note,
                        amount = expense.amount,
                        date = expense.date,
                        category = BudgetCategory(
                            id = expense.categoryId,
                            budgetId = expense.budgetId,
                            name = categoryName.name
                        )
                    )
                }
            }
    }

    override suspend fun editExpense(expense: Expense): Expense? =
        db.runInTransaction {
            val old = getExpenseById(expense.id) ?: return@runInTransaction null
            val category = getBudgetCategoryEntityById(expense.categoryId) ?: return@runInTransaction null
            val budget = getBudgetEntityById(expense.budgetId) ?: return@runInTransaction null
            val diff = expense.amount - old.amount

            db.expenseDao.update(expense.toEntity())
            db.budgetCategoryDao.update(
                category.copy(
                    totalExpense = category.totalExpense + diff, lastModified = System.currentTimeMillis()
                )
            )
            db.budgetDao.update(
                budget.copy(
                    totalExpense = budget.totalExpense + diff, lastModified = System.currentTimeMillis()
                )
            )

            expense.copy()
        }

    override suspend fun removeExpense(expense: Expense, reverseAmounts: Boolean) {
        db.runInTransaction {
            val old = getExpenseById(expense.id) ?: return@runInTransaction
            val category = getBudgetCategoryEntityById(expense.categoryId) ?: return@runInTransaction
            val budget = getBudgetEntityById(expense.budgetId) ?: return@runInTransaction

            db.expenseDao.delete(expense.toEntity())

            db.budgetCategoryDao.update(
                category.copy(
                    totalExpense = category.totalExpense - old.amount, lastModified = System.currentTimeMillis()
                )
            )

            db.budgetDao.update(
                budget.copy(
                    totalExpense = budget.totalExpense - old.amount, lastModified = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun removeMultipleExpenses(expenses: List<Expense>, reverseAmounts: Boolean) = db.runInTransaction {
            for (expense in expenses) {
                removeExpense(expense, reverseAmounts)
            }
        }

    private suspend fun getBudgetEntityById(id: Long): BudgetEntity? =
        db.budgetDao.observeBudgetById(id).first()

    private suspend fun getBudgetCategoryEntityById(id: Long): BudgetCategoryEntity? =
        db.budgetCategoryDao.observeCategoryById(id).first()

    private suspend fun getExpenseById(id: Long): ExpenseEntity? =
        db.expenseDao.observeExpenseById(id).first()
}