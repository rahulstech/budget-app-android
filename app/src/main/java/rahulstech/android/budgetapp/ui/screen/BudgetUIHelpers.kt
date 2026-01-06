package rahulstech.android.budgetapp.ui.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import java.util.UUID

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

fun BudgetCategory.toBudgetCategoryParcel(key: String = UUID.randomUUID().toString()): BudgetCategoryParcelable =
    BudgetCategoryParcelable(
        key = key,
        categoryId = id,
        budgetId = budgetId,
        name = name,
        note = note,
        allocation = allocation
    )