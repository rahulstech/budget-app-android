package rahulstech.android.budgetapp.budgetdb.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import rahulstech.android.budgetapp.budgetdb.entity.ExpenseEntity
import rahulstech.android.budgetapp.budgetdb.model.ExpenseWithCategoryModel
import java.time.LocalDate

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Query("SELECT * FROM `expenses` WHERE `id` = :id")
    fun observeExpenseById(id: Long): Flow<ExpenseEntity?>

    fun observeExpenses(budgetId: Long,
                        categories: List<Long> = emptyList(),
                        dateRange: Pair<LocalDate, LocalDate>? = null,
                        newestFirst: Boolean = true): PagingSource<Int, ExpenseWithCategoryModel> {
        return if (categories.isEmpty() && null == dateRange) {
            observeExpensesOfBudget(budgetId, newestFirst)
        }
        else if (null == dateRange) {
            observeExpensesOfBudgetOfCategories(budgetId,categories,newestFirst)
        }
        else if (categories.isEmpty()) {
            observeExpensesOfBudgetBetweenDates(budgetId,dateRange.first,dateRange.second, newestFirst)
        }
        else {
            observeExpensesOfBudgetOfCategoriesBetweenDates(budgetId,categories,dateRange.first,dateRange.second, newestFirst)
        }
    }

    fun observeExpensesOfBudget(budgetId: Long, newestFirst: Boolean = true):PagingSource<Int, ExpenseWithCategoryModel> =
        if (newestFirst) observeExpensesOfBudgetNewestFirst(budgetId)
        else observeExpensesOfBudgetOldestFirst(budgetId)

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId ORDER BY `date` ASC")
    fun observeExpensesOfBudgetOldestFirst(budgetId: Long):PagingSource<Int, ExpenseWithCategoryModel>

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId ORDER BY `date` DESC")
    fun observeExpensesOfBudgetNewestFirst(budgetId: Long):PagingSource<Int, ExpenseWithCategoryModel>


    fun observeExpensesOfBudgetOfCategories(budgetId: Long,
                                            categoryIds: List<Long>,
                                            newestFirst: Boolean = true): PagingSource<Int, ExpenseWithCategoryModel> =
        if (newestFirst) observeExpensesOfBudgetOfCategoriesNewestFirst(budgetId,categoryIds)
        else observeExpensesOfBudgetOfCategoriesOldestFirst(budgetId,categoryIds)

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `categoryId` IN(:categoryIds) ORDER BY `date` DESC")
    fun observeExpensesOfBudgetOfCategoriesNewestFirst(budgetId: Long, categoryIds: List<Long>): PagingSource<Int, ExpenseWithCategoryModel>

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `categoryId` IN(:categoryIds) ORDER BY `date` ASC")
    fun observeExpensesOfBudgetOfCategoriesOldestFirst(budgetId: Long, categoryIds: List<Long>): PagingSource<Int, ExpenseWithCategoryModel>

    fun observeExpensesOfBudgetBetweenDates(budgetId: Long,
                                            startInclusive: LocalDate, endInclusive: LocalDate,
                                            newestFirst: Boolean = true): PagingSource<Int, ExpenseWithCategoryModel> =
        if (newestFirst) observeExpensesOfBudgetBetweenDatesNewestFirst(budgetId, startInclusive, endInclusive)
        else observeExpensesOfBudgetBetweenDatesOldestFirst(budgetId, startInclusive, endInclusive)

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `date` BETWEEN :startInclusive AND :endInclusive ORDER BY `date` DESC")
    fun observeExpensesOfBudgetBetweenDatesNewestFirst(budgetId: Long,
                                            startInclusive: LocalDate, endInclusive: LocalDate,
    ): PagingSource<Int, ExpenseWithCategoryModel>

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `date` BETWEEN :startInclusive AND :endInclusive ORDER BY `date` ASC")
    fun observeExpensesOfBudgetBetweenDatesOldestFirst(budgetId: Long,
                                            startInclusive: LocalDate, endInclusive: LocalDate,
    ): PagingSource<Int, ExpenseWithCategoryModel>



    fun observeExpensesOfBudgetOfCategoriesBetweenDates(budgetId: Long, categoryIds: List<Long>,
                                                        startInclusive: LocalDate, endInclusive: LocalDate,
                                                        newestFirst: Boolean = true): PagingSource<Int, ExpenseWithCategoryModel> =
        if (newestFirst) observeExpensesOfBudgetOfCategoriesBetweenDatesNewestFirst(budgetId,categoryIds,startInclusive,endInclusive)
        else observeExpensesOfBudgetOfCategoriesBetweenDatesOldestFirst(budgetId,categoryIds,startInclusive,endInclusive)

    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `date` BETWEEN :startInclusive AND :endInclusive AND `categoryId` IN(:categoryIds)" +
            " ORDER BY `date` DESC")
    fun observeExpensesOfBudgetOfCategoriesBetweenDatesNewestFirst(budgetId: Long, categoryIds: List<Long>,
                                                        startInclusive: LocalDate, endInclusive: LocalDate,
    ): PagingSource<Int, ExpenseWithCategoryModel>


    @Transaction
    @Query("SELECT `id`,`budgetId`,`categoryId`,`amount`,`note`,`date` FROM `expenses` " +
            "WHERE `budgetId` = :budgetId AND `date` BETWEEN :startInclusive AND :endInclusive AND `categoryId` IN(:categoryIds) " +
            "ORDER BY `date` ASC")
    fun observeExpensesOfBudgetOfCategoriesBetweenDatesOldestFirst(budgetId: Long, categoryIds: List<Long>,
                                                        startInclusive: LocalDate, endInclusive: LocalDate,
                                                        ): PagingSource<Int, ExpenseWithCategoryModel>

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpenses(expenses: List<ExpenseEntity>)
}