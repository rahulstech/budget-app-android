package rahulstech.android.budgetapp.budgetdb.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.budgetdb.entity.BudgetEntity
import rahulstech.android.budgetapp.budgetdb.model.BudgetListModel

@Dao
interface BudgetDao {

    @Insert
    suspend fun insert(budget: BudgetEntity): Long

    @Query("SELECT * FROM `budgets` WHERE `id` = :id")
    fun observeBudgetById(id: Long): Flow<BudgetEntity?>

    @Query("SELECT `id`,`name`,`totalAllocation`,`totalExpense` FROM `budgets`")
    fun observerAllBudgets(): PagingSource<Int, BudgetListModel>

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    fun delete(budget: BudgetEntity)
}