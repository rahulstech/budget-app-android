package rahulstech.android.budgetapp.repository.model

import java.time.LocalDate

data class Expense(
    val id: String = "",
    val budgetId: String,
    val categoryId: String,
    val amount: Double = 0.0,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
)
