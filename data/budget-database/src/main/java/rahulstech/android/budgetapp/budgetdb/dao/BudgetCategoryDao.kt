package rahulstech.android.budgetapp.budgetdb.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.budgetdb.entity.BudgetCategoryEntity
import rahulstech.android.budgetapp.budgetdb.model.BudgetCategoryModel

@Dao
interface BudgetCategoryDao {

    @Insert
    suspend fun insert(category: BudgetCategoryEntity): Long

    @Query("SELECT * FROM `categories` WHERE `id`= :id")
    fun observeCategoryById(id: Long): Flow<BudgetCategoryEntity?>

    @Query("SELECT `id`,`budgetId`,`name`, `note`, `allocation`, `totalExpense` FROM `categories` WHERE `budgetId` = :budgetId")
    fun observeCategoriesOfBudget(budgetId: Long): Flow<List<BudgetCategoryModel>>

    @Update
    suspend fun update(category: BudgetCategoryEntity)

    @Delete
    suspend fun delete(category: BudgetCategoryEntity)
}