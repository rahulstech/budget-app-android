package rahulstech.android.budgetapp.budgetdb.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val details: String = "",
    val totalAllocation: Double,
    val totalExpense: Double,
    val lastModified: Long = System.currentTimeMillis(),
)