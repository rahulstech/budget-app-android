package rahulstech.android.budgetapp.budgetdb.fakedata

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import rahulstech.android.budgetapp.budgetdb.Converters
import rahulstech.android.budgetapp.budgetdb.epochPlusDays
import java.time.LocalDate

class FakeDataV1: RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        // budgets
        insertBudgets(db, 12)

        // categories
        insertBudgetCategories(db, 1, 4)
        insertBudgetCategories(db, 2, 2)

        // expenses
        insertExpenses(db, 1, 101, 50)
        insertExpenses(db, 2, 202, 20)
    }

    fun insertBudgets(db: SupportSQLiteDatabase, count: Int) {

        (1..count).forEach { position ->
            db.execSQL("INSERT INTO `budgets` (`id`,`name`,`details`,`totalAllocation`,`totalExpense`,`lastModified`) " +
                    "VALUES ($position,'Budget $position','details of Budget $position',${position * 500},${position * 300}," +
                    "${ 1768033905000L + position * 1000L })")
        }
    }

    fun insertBudgetCategories(db: SupportSQLiteDatabase, budgetId: Int, count: Int) {
        (1..count).forEach { position ->
            db.execSQL("INSERT INTO `categories` (`id`,`budgetId`, `name`, `note`, `allocation`, `totalExpense`, `lastModified`) " +
                    "VALUES (${budgetId * 100 + position}, $budgetId, 'Category $budgetId-$position', 'note for Category $budgetId-$position'," +
                    "${position * 500}, ${position * 300},${1768034005000L + position * 1500L })")
        }
    }

    fun insertExpenses(db: SupportSQLiteDatabase, budgetId: Int, categoryId: Int, count: Int) {
        (1..count).forEach { position ->
            db.execSQL("INSERT INTO `expenses` (`id`,`budgetId`, `categoryId`, `note`, `amount`, `date`, `lastModified`) " +
                    "VALUES (${budgetId * 100000 + categoryId * 100 + position}, $budgetId, $categoryId, " +
                    "'note for expense expense $position of budget $budgetId and category $categoryId'," +
                    "${position * 100},${epochPlusDays(position)}, ${1768034105000L + position * 1300L })")
        }
    }
}