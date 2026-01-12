package rahulstech.android.budgetapp.budgetdb.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import rahulstech.android.budgetapp.budgetdb.BudgetDB
import rahulstech.android.budgetapp.budgetdb.createInMemoryBudgetDB
import rahulstech.android.budgetapp.budgetdb.entity.BudgetCategoryEntity
import rahulstech.android.budgetapp.budgetdb.fakeData
import rahulstech.android.budgetapp.budgetdb.model.BudgetCategoryModel

@RunWith(AndroidJUnit4::class)
class BudgetCategoryDaoTest {

    lateinit var db: BudgetDB
    lateinit var dao: BudgetCategoryDao

    @Before
    fun setUp() {
        db = createInMemoryBudgetDB(fakeData())
        dao = db.budgetCategoryDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert() = runBlocking {
        val category = BudgetCategoryEntity(
            budgetId = 1,
            name = "Test Category",
            note = "note for Test Category of Budget 1",
            allocation = 1000.0,
            totalExpense = 0.0,
        )

        val id = dao.insert(category)
        assertTrue(id > 0)

        val actual = dao.observeCategoryById(id).first()
        val expected = category.copy(id = id)
        assertEquals(expected, actual)
    }

    @Test
    fun observeAllCategoriesOfBudget() = runBlocking{
        val budgetId = 1L
        val categories = dao.observeCategoriesOfBudget(budgetId).first()

        val expectedSize = 4
        assertEquals(expectedSize, categories.size)

        val expected = (1..expectedSize).map { position ->
            BudgetCategoryModel(
                id = budgetId * 100 + position,
                budgetId = budgetId,
                name = "Category $budgetId-$position",
                note = "note for Category $budgetId-$position",
                allocation = position * 500.0,
                totalExpense = position * 300.0
            )
        }

        assertEquals(expected, categories)
    }
}