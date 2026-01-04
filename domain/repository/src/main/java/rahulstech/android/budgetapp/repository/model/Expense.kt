package rahulstech.android.budgetapp.repository.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Expense(
    val id: String = "",
    val budgetId: String,
    val categoryId: String,
    val amount: Double = 0.0,
    val datetime: LocalDateTime = LocalDateTime.now(),
    val note: String = "",
)
