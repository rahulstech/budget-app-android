package rahulstech.android.budgetapp.budgetdb.model

import androidx.room.Embedded
import androidx.room.Relation
import rahulstech.android.budgetapp.budgetdb.entity.BudgetCategoryEntity
import java.time.LocalDate

data class ExpenseModel(
    val id: Long,
    val budgetId: Long,
    val categoryId: Long,
    val note: String,
    val amount: Double,
    val date: LocalDate
)

data class ExpenseWithCategoryModel(
    @Embedded
    val expense: ExpenseModel,

    @Relation(
        entity = BudgetCategoryEntity::class,
        entityColumn = "id",
        parentColumn = "categoryId",
        projection = ["id", "budgetId", "name"]
    )
    val categoryName: BudgetCategoryNameModel
)