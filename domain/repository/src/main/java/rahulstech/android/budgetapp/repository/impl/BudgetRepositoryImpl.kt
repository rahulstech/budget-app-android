package rahulstech.android.budgetapp.repository.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import rahulstech.android.budgetapp.budgetdb.IBudgetDB
import rahulstech.android.budgetapp.budgetdb.entity.BudgetEntity
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.repository.toEntity
import rahulstech.android.budgetapp.repository.toModel

class BudgetRepositoryImpl(val db: IBudgetDB): BudgetRepository {

    override suspend fun createBudget(budget: Budget): Budget =
        db.runInTransaction<Budget> {

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
        db.budgetDao.observeBudgetById(id).map { it?.toModel() }

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

    override suspend fun editBudget(budget: Budget): Budget?  {
        val existing = db.budgetDao
            .observeBudgetById(budget.id)
            .first()
            ?: return null

        val updated = existing.copy(
            name = budget.name,
            details = budget.details,
            lastModified = System.currentTimeMillis()
        )

        db.budgetDao.update(updated)

        return budget.copy(
            totalAllocation = existing.totalAllocation,
            totalExpense = existing.totalExpense
        )
    }

    override suspend fun removeBudget(budget: Budget) {
        db.budgetDao.delete(budget.toEntity())
    }

    override suspend fun addCategory(category: BudgetCategory): BudgetCategory =
        db.runInTransaction<BudgetCategory> {

            val budget = db.budgetDao
                .observeBudgetById(category.budgetId)
                .first() ?: return@runInTransaction category // TODO: throw budget not found

            val id = db.budgetCategoryDao.insert(category.copy(id = 0).toEntity())

            db.budgetDao.update(
                budget.copy(
                    totalAllocation = budget.totalAllocation + category.allocation
                )
            )

            category.copy(id = id)
        }

    override fun observeBudgetCategoriesForBudget(budgetId: Long): Flow<List<BudgetCategory>> {
        return db.budgetCategoryDao
            .observeCategoriesOfBudget(budgetId)
            .map { list ->
                list.map { model ->
                    BudgetCategory(
                        id = model.id,
                        budgetId = model.budgetId,
                        name = model.name,
                        allocation = model.allocation,
                        totalExpense = model.totalExpense
                    )
                }
            }
    }

    override suspend fun editCategory(category: BudgetCategory): BudgetCategory? =
        db.runInTransaction<BudgetCategory?> {
            val old = db.budgetCategoryDao
                .observeCategoryById(category.id)
                .first() ?: return@runInTransaction null

            val allocationDiff = category.allocation - old.allocation

            db.budgetCategoryDao.update(category.toEntity())

            val budget = db.budgetDao
                .observeBudgetById(category.budgetId)
                .first() ?: return@runInTransaction category

            db.budgetDao.update(
                budget.copy(
                    totalAllocation = budget.totalAllocation + allocationDiff
                )
            )

            category.copy()
        }

    override suspend fun removeCategory(category: BudgetCategory, reverseAmounts: Boolean) {
        db.runInTransaction {
            val old = db.budgetCategoryDao
                .observeCategoryById(category.id)
                .first() ?: return@runInTransaction

            db.budgetCategoryDao.delete(category.toEntity())

            if (reverseAmounts) {
                val budget = db.budgetDao
                    .observeBudgetById(category.budgetId)
                    .first() ?: return@runInTransaction

                db.budgetDao.update(
                    budget.copy(
                        totalAllocation = budget.totalAllocation - category.allocation,
                        totalExpense = budget.totalExpense - category.totalExpense
                    )
                )
            }
        }
    }

    override suspend fun addExpense(expense: Expense): Expense =
        db.runInTransaction<Expense> {
            val budget = db.budgetDao
                .observeBudgetById(expense.budgetId)
                .first() ?: return@runInTransaction expense // TODO: throw budget not found

            val category = db.budgetCategoryDao
                .observeCategoryById(expense.categoryId)
                .first() ?: return@runInTransaction expense // TODO: throw category not found

            val id = db.expenseDao.insert(expense.toEntity())

            db.budgetCategoryDao.update(
                category.copy(
                    totalExpense = category.totalExpense + expense.amount
                )
            )

            db.budgetDao.update(
                budget.copy(
                    totalExpense = budget.totalExpense + expense.amount
                )
            )

            expense.copy(id = id)
        }

    override suspend fun editExpense(expense: Expense): Expense? =
        db.runInTransaction<Expense?> {
            val old = db.expenseDao
                .observeExpenseById(expense.id)
                .first() ?: return@runInTransaction null

            val diff = expense.amount - old.amount

            db.expenseDao.update(expense.copy(id = 0).toEntity())
            val copy = expense.copy()

            // update totalExpense in BudgetCategory
            val category = db.budgetCategoryDao
                .observeCategoryById(expense.categoryId)
                .first() ?: return@runInTransaction copy

            db.budgetCategoryDao.update(
                category.copy(totalExpense = category.totalExpense + diff)
            )

            // update totalExpense in Budget
            val budget = db.budgetDao
                .observeBudgetById(expense.budgetId)
                .first() ?: return@runInTransaction copy

            db.budgetDao.update(
                budget.copy(totalExpense = budget.totalExpense + diff)
            )

            copy
        }

    override suspend fun removeExpense(expense: Expense, reverseAmounts: Boolean) {
        db.runInTransaction {
            val old = db.expenseDao.observeExpenseById(expense.id)
                .first() ?: return@runInTransaction

            db.expenseDao.delete(expense.toEntity())

            val category = db.budgetCategoryDao
                .observeCategoryById(expense.categoryId)
                .first() ?: return@runInTransaction

            db.budgetCategoryDao.update(
                category.copy(
                    totalExpense = category.totalExpense - old.amount
                )
            )

            val budget = db.budgetDao
                .observeBudgetById(expense.budgetId)
                .first() ?: return@runInTransaction

            db.budgetDao.update(
                budget.copy(
                    totalExpense = budget.totalExpense - old.amount
                )
            )
        }
    }

}