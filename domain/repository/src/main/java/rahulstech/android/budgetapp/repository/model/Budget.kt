package rahulstech.android.budgetapp.repository.model

import java.math.BigDecimal

data class Budget(
    val id: String = "",
    val name: String,
    val details: String = "",
    val totalAllocation: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categories: List<BudgetCategory> = emptyList(),
)
