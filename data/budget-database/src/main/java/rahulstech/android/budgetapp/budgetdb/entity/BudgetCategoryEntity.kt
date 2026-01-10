package rahulstech.android.budgetapp.budgetdb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import rahulstech.android.budgetapp.budgetdb.Converters
import java.time.LocalDateTime

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(
            value = ["budgetId"],
            name = "index_categories_budgetId"
        )
    ]
)
data class BudgetCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val budgetId: Long,
    val name: String,
    val note: String = "",
    val allocation: Double = 0.0,
    val totalExpense: Double = 0.0,
    val lastModified: Long = System.currentTimeMillis(),
)
