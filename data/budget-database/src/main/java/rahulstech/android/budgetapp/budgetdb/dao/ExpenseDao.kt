package rahulstech.android.budgetapp.budgetdb.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.budgetdb.Converters
import rahulstech.android.budgetapp.budgetdb.entity.ExpenseEntity
import rahulstech.android.budgetapp.budgetdb.model.ExpenseWithCategoryModel
import java.time.LocalDate

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Query("SELECT * FROM `expenses` WHERE `id` = :id")
    fun observeExpenseById(id: Long): Flow<ExpenseEntity?>

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` WHERE `budgetId` = :budgetId")
    fun observeExpensesOfBudget(budgetId: Long):PagingSource<Int, ExpenseWithCategoryModel>

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `categoryId` IN(:categoryIds)")
    fun observeExpensesOfBudgetOfCategories(budgetId: Long, categoryIds: List<Long>): PagingSource<Int, ExpenseWithCategoryModel>

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `date` BETWEEN :startInclusive AND :endInclusive")
    fun observeExpensesOfBudgetBetweenDates(budgetId: Long,
                                            startInclusive: LocalDate, endInclusive: LocalDate,
                                            ): PagingSource<Int, ExpenseWithCategoryModel>

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `date` BETWEEN :startInclusive AND :endInclusive AND `categoryId` IN(:categoryIds)")
    fun observeExpensesOfBudgetOfCategoriesBetweenDates(budgetId: Long, categoryIds: List<Long>,
                                                        startInclusive: LocalDate, endInclusive: LocalDate,
                                                        ): PagingSource<Int, ExpenseWithCategoryModel>

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}