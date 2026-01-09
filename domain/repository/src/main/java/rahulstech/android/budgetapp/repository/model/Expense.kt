package rahulstech.android.budgetapp.repository.model

import java.time.LocalDate

data class Expense(
    val id: Long = 0,
    val budgetId: Long,
    val categoryId: Long,
    val amount: Double = 0.0,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val budget: Budget? = null,
    val category: BudgetCategory? = null
)
