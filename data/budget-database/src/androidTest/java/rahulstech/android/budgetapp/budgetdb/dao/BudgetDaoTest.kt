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
import rahulstech.android.budgetapp.budgetdb.createInMemoryBudgetDB
import rahulstech.android.budgetapp.budgetdb.entity.BudgetEntity
import rahulstech.android.budgetapp.budgetdb.fakeData
import rahulstech.android.budgetapp.budgetdb.model.BudgetListModel

@RunWith(AndroidJUnit4::class)
class BudgetDaoTest {

    lateinit var db: BudgetDB
    lateinit var dao: BudgetDao

    @Before
    fun setUp() {
        db = createInMemoryBudgetDB(fakeData())
        dao = db.budgetDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert() = runBlocking {
        val budget = BudgetEntity(
            name = "Test Budget 1",
            details = "Details of Test Budget 1",
            totalAllocation = 1000.0,
            totalExpense = 1000.0,
        )

        val id = dao.insert(budget)

        assertTrue(id > 0)

        val actualBudget = dao.observeBudgetById(id).first()

        assertNotNull(actualBudget)

        val expectedBudget = budget.copy(id = id)
        assertEquals(expectedBudget, actualBudget)
    }

    @Test
    fun observerAllBudgets() = runBlocking {

        val source = dao.observerAllBudgets()

        val params1 = PagingSource.LoadParams.Refresh<Int>(
            key = null, // offset
            loadSize = 5, // limit
            placeholdersEnabled = false
        )

        val result = source.load(params1)

        assertTrue(result is PagingSource.LoadResult.Page)

        // test for first page

        val page1 = result as PagingSource.LoadResult.Page

        assertEquals(5, page1.data.size)

        val budget1 = BudgetListModel(
            id = 1,
            name = "Budget 1",
            totalAllocation = 500.0,
            totalExpense = 300.0
        )
        val budget5 = BudgetListModel(
            id = 5,
            name = "Budget 5",
            totalAllocation = 2500.0,
            totalExpense = 1500.0
        )

        assertEquals(budget1, page1.data.first())
        assertEquals(budget5, page1.data.last())

        assertNull(page1.prevKey)
        assertEquals(5, page1.nextKey)

        // test for second page

        val nextParams = PagingSource.LoadParams.Append(
            key = page1.nextKey!!, // offset
            loadSize = 5, // limit
            placeholdersEnabled = false
        )

        val page2 = source.load(nextParams) as PagingSource.LoadResult.Page

        assertEquals(5, page2.data.size)
        val budget6 = BudgetListModel(
            id = 6,
            name = "Budget 6",
            totalAllocation = 3000.0,
            totalExpense = 1800.0
        )
        val budget10 = BudgetListModel(
            id = 10,
            name = "Budget 10",
            totalAllocation = 5000.0,
            totalExpense = 3000.0
        )

        assertEquals(budget6, page2.data.first())
        assertEquals(budget10, page2.data.last())
    }
}