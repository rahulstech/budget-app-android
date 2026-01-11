package rahulstech.android.budgetapp.budgetdb.dao

import androidx.paging.PagingSource
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rahulstech.android.budgetapp.budgetdb.BudgetDB
import rahulstech.android.budgetapp.budgetdb.Converters
import rahulstech.android.budgetapp.budgetdb.createInMemoryBudgetDB
import rahulstech.android.budgetapp.budgetdb.entity.ExpenseEntity
import rahulstech.android.budgetapp.budgetdb.epochPlusDays
import rahulstech.android.budgetapp.budgetdb.fakeData
import rahulstech.android.budgetapp.budgetdb.model.BudgetCategoryNameModel
import rahulstech.android.budgetapp.budgetdb.model.ExpenseModel
import rahulstech.android.budgetapp.budgetdb.model.ExpenseWithCategoryModel
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    lateinit var db: BudgetDB

    lateinit var dao: ExpenseDao

    @Before
    fun setUp() {
        db = createInMemoryBudgetDB(fakeData())
        dao = db.expenseDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert() = runBlocking {
        val expense = ExpenseEntity(
            budgetId = 1,
            categoryId = 102,
            note = "test expense",
            amount = 100.0,
            date = LocalDate.now()
        )

        val id = dao.insert(expense)
        assertTrue(id > 0)

        val actual = dao.observeExpenseById(id).first()
        val expected = expense.copy(id = id)
        assertEquals(expected, actual)
    }

    @Test
    fun observeExpensesOfBudget() = runBlocking {
        val source = dao.observeExpensesOfBudget(1, newestFirst = false)

        val params1 = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )
        val result1 = source.load(params1)

        assertTrue(result1 is PagingSource.LoadResult.Page)

        val page1 = result1 as PagingSource.LoadResult.Page
        assertEquals(10, page1.data.size)

        val expense1 = ExpenseWithCategoryModel(
            expense = ExpenseModel(
                id = 110101,
                budgetId = 1,
                categoryId = 101,
                note = "note for expense expense 1 of budget 1 and category 101",
                amount = 100.0,
                date = Converters.longToLocalDate(epochPlusDays(1))!!
            ),
            categoryName = BudgetCategoryNameModel(
                id = 101,
                budgetId = 1,
                name = "Category 1-1"
            )
        )
        val expense10 = ExpenseWithCategoryModel(
            expense = ExpenseModel(
                id = 110110,
                budgetId = 1,
                categoryId = 101,
                note = "note for expense expense 10 of budget 1 and category 101",
                amount = 1000.0,
                date = Converters.longToLocalDate(epochPlusDays(10))!!
            ),
            categoryName = BudgetCategoryNameModel(
                id = 101,
                budgetId = 1,
                name = "Category 1-1"
            )
        )
        assertEquals(expense1, page1.data.first())
        assertEquals(expense10, page1.data.last())
        assertNotNull(page1.nextKey)
    }

    @Test
    fun observeExpensesOfBudgetBetweenDatesNewestFirst() = runBlocking {
        val source = dao.observeExpensesOfBudgetBetweenDatesNewestFirst(
            budgetId = 1,
            startInclusive = Converters.longToLocalDate(epochPlusDays(1))!!,
            endInclusive = Converters.longToLocalDate(epochPlusDays(5))!!
        )

        val params1 = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )
        val result1 = source.load(params1)
        assertTrue(result1 is PagingSource.LoadResult.Page)

        val page1 = result1 as PagingSource.LoadResult.Page
        assertEquals(5, page1.data.size)

        val expenseLast = ExpenseWithCategoryModel(
            expense = ExpenseModel(
                id = 110101,
                budgetId = 1,
                categoryId = 101,
                note = "note for expense expense 1 of budget 1 and category 101",
                amount = 100.0,
                date = Converters.longToLocalDate(epochPlusDays(1))!!
            ),
            categoryName = BudgetCategoryNameModel(
                id = 101,
                budgetId = 1,
                name = "Category 1-1"
            )
        )
        val expenseFirst = ExpenseWithCategoryModel(
            expense = ExpenseModel(
                id = 110105,
                budgetId = 1,
                categoryId = 101,
                note = "note for expense expense 5 of budget 1 and category 101",
                amount = 500.0,
                date = Converters.longToLocalDate(epochPlusDays(5))!!
            ),
            categoryName = BudgetCategoryNameModel(
                id = 101,
                budgetId = 1,
                name = "Category 1-1"
            )
        )
        assertEquals(expenseFirst, page1.data.first())
        assertEquals(expenseLast, page1.data.last())
        assertNull(page1.nextKey)
    }

    @Test
    fun observeExpensesOfBudgetBetweenDatesOldestFirst() = runBlocking {
        val source = dao.observeExpensesOfBudgetBetweenDatesOldestFirst(
            budgetId = 1,
            startInclusive = Converters.longToLocalDate(epochPlusDays(1))!!,
            endInclusive = Converters.longToLocalDate(epochPlusDays(5))!!
        )

        val params1 = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )
        val result1 = source.load(params1)
        assertTrue(result1 is PagingSource.LoadResult.Page)

        val page1 = result1 as PagingSource.LoadResult.Page
        assertEquals(5, page1.data.size)

        val expenseFirst = ExpenseWithCategoryModel(
            expense = ExpenseModel(
                id = 110101,
                budgetId = 1,
                categoryId = 101,
                note = "note for expense expense 1 of budget 1 and category 101",
                amount = 100.0,
                date = Converters.longToLocalDate(epochPlusDays(1))!!
            ),
            categoryName = BudgetCategoryNameModel(
                id = 101,
                budgetId = 1,
                name = "Category 1-1"
            )
        )
        val expenseLast = ExpenseWithCategoryModel(
            expense = ExpenseModel(
                id = 110105,
                budgetId = 1,
                categoryId = 101,
                note = "note for expense expense 5 of budget 1 and category 101",
                amount = 500.0,
                date = Converters.longToLocalDate(epochPlusDays(5))!!
            ),
            categoryName = BudgetCategoryNameModel(
                id = 101,
                budgetId = 1,
                name = "Category 1-1"
            )
        )
        assertEquals(expenseFirst, page1.data.first())
        assertEquals(expenseLast, page1.data.last())
        assertNull(page1.nextKey)
    }
}