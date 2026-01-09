package rahulstech.android.budgetapp.repository.model

data class Budget(
    val id: Long = 0,
    val name: String,
    val details: String = "",
    val totalAllocation: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categories: List<BudgetCategory> = emptyList(),
)
