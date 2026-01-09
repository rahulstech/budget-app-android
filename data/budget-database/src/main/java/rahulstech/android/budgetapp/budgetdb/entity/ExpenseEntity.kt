package rahulstech.android.budgetapp.budgetdb.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverters
import rahulstech.android.budgetapp.budgetdb.Converters
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BudgetCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(
            value = ["budgetId"],
            name = "index_expenses_budgetId"
        ),
        Index(
            value = ["categoryId"],
            name = "index_expenses_categoryId"
        )
    ]
)
data class ExpenseEntity(
    val id: Long,
    val budgetId: Long,
    val categoryId: Long,
    val amount: Double,
    @TypeConverters(Converters::class)
    val date: LocalDate,
    val note: String? = null,
    @TypeConverters(Converters::class)
    val lastModified: LocalDateTime = LocalDateTime.now(),
)