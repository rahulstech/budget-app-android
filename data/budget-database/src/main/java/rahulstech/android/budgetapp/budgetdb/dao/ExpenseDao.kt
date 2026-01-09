package rahulstech.android.budgetapp.budgetdb.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.budgetdb.entity.ExpenseEntity
import rahulstech.android.budgetapp.budgetdb.model.ExpenseWithCategoryModel
import java.time.LocalDate

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` WHERE `budgetId` = :budgetId")
    fun observeExpensesOfBudget(budgetId: Long): Flow<PagingSource<Int, ExpenseWithCategoryModel>>

    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` WHERE `categoryId` IN(:categoryIds)")
    fun observeExpensesOfCategories(categoryIds: List<Long>): Flow<PagingSource<Int, ExpenseWithCategoryModel>>

    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `date` BETWEEN :startInclusive AND :endInclusive")
    fun observeExpensesBetweenDates(startInclusive: LocalDate, endInclusive: LocalDate): Flow<PagingSource<Int, ExpenseWithCategoryModel>>

    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `date` BETWEEN :startInclusive AND :endInclusive AND `categoryId` IN(:categoryIds)")
    fun observeExpensesOfCategoriesBetweenDates(categoryIds: List<Long>,
                                                startInclusive: LocalDate, endInclusive: LocalDate
                                                ): Flow<PagingSource<Int, ExpenseWithCategoryModel>>

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}