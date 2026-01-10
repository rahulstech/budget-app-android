package rahulstech.android.budgetapp.repository

import rahulstech.android.budgetapp.budgetdb.entity.BudgetCategoryEntity
import rahulstech.android.budgetapp.budgetdb.entity.BudgetEntity
import rahulstech.android.budgetapp.budgetdb.entity.ExpenseEntity
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.repository.model.Expense

internal fun Budget.toEntity() = BudgetEntity(
    id = id,
    name = name,
    details = details,
    totalAllocation = totalAllocation,
    totalExpense = totalExpense
)

internal fun BudgetEntity.toModel() = Budget(
    id = id,
    name = name,
    details = details,
    totalAllocation = totalAllocation,
    totalExpense = totalExpense
)

internal fun BudgetCategory.toEntity() = BudgetCategoryEntity(
    id = id,
    budgetId = budgetId,
    name = name,
    note = note,
    allocation = allocation,
    totalExpense = totalExpense
)

internal fun BudgetCategoryEntity.toModel() = BudgetCategory(
    id = id,
    budgetId = budgetId,
    name = name,
    note = note,
    allocation = allocation,
    totalExpense = totalExpense
)

internal fun Expense.toEntity() = ExpenseEntity(
    id = id,
    budgetId = budgetId,
    categoryId = categoryId,
    amount = amount,
    date = date,
    note = note
)

internal fun ExpenseEntity.toModel() = Expense(
    id = id,
    budgetId = budgetId,
    categoryId = categoryId,
    amount = amount,
    date = date,
    note = note ?: ""
)
