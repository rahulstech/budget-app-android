package rahulstech.android.budgetapp.budgetdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import rahulstech.android.budgetapp.budgetdb.Converters
import java.time.LocalDateTime

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val details: String,
    val totalAllocation: Double,
    val totalExpense: Double,
    @TypeConverters(Converters::class)
    val lastModified: LocalDateTime = LocalDateTime.now(),
)