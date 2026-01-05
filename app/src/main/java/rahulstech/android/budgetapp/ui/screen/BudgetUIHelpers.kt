package rahulstech.android.budgetapp.ui.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import java.util.UUID

fun Budget.calculateExpenseProgress(): Float =
    (totalExpense / totalAllocation).coerceIn(0.0,1.0).toFloat()

@Parcelize
data class BudgetCategoryParcelable(
    val key: String = UUID.randomUUID().toString(),
    val categoryId: String = "",
    val budgetId: String = "",
    val name: String,
    val note: String,
    val allocation: Double
): Parcelable {

    fun toBudgetCategory(): BudgetCategory =
        BudgetCategory(
            id = categoryId,
            budgetId = budgetId,
            name = name,
            note = note,
            allocation = allocation
        )
}

fun BudgetCategory.calculateExpenseProgress(): Float =
    (totalExpense / allocation).coerceIn(0.0, 1.0).toFloat()