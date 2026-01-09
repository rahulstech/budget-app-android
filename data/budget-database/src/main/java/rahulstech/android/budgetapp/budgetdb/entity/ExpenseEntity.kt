package rahulstech.android.budgetapp.budgetdb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import rahulstech.android.budgetapp.budgetdb.Converters
import java.time.LocalDate

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val budgetId: Long,
    val categoryId: Long,
    val amount: Double = 0.0,
    @TypeConverters(Converters::class)
    @ColumnInfo(typeAffinity = ColumnInfo.INTEGER)
    val date: LocalDate = LocalDate.now(),
    val note: String? = null,
    val lastModified: Long = System.currentTimeMillis(),
)