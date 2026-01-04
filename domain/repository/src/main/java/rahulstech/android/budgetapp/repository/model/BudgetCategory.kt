package rahulstech.android.budgetapp.repository.model

import java.math.BigDecimal

data class BudgetCategory(
    val id: String = "",
    val budgetId: String,
    val name: String,
    val note: String = "",
    val allocation: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expenses: List<Expense> = emptyList(),
)
