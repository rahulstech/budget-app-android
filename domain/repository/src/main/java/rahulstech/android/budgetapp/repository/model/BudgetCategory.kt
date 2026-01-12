package rahulstech.android.budgetapp.repository.model

data class BudgetCategory(
    val id: Long = 0,
    val budgetId: Long,
    val name: String,
    val allocation: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expenses: List<Expense> = emptyList(),
    val budget: Budget? = null
)
