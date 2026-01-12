package rahulstech.android.budgetapp.budgetdb.model

data class BudgetCategoryNameModel(
    val id: Long,
    val budgetId: Long,
    val name: String,
)

data class BudgetCategoryModel(
    val id: Long,
    val budgetId: Long,
    val name: String,
    val note: String,
    val allocation: Double,
    val totalExpense: Double,
)