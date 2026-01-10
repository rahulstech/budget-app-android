package rahulstech.android.budgetapp.budgetdb.model

import rahulstech.android.budgetapp.budgetdb.entity.BudgetEntity

data class BudgetListModel(
    val id: Long,
    val name: String,
    val totalAllocation: Double,
    val totalExpense: Double,
)

data class BudgetModel(
    val id: Long,
    val name: String,
    val details: String,
    val totalAllocation: Double,
    val totalExpense: Double
)

