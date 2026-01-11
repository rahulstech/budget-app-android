package rahulstech.android.budgetapp.budgetdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import kotlinx.coroutines.coroutineScope
import rahulstech.android.budgetapp.budgetdb.dao.BudgetCategoryDao
import rahulstech.android.budgetapp.budgetdb.dao.BudgetDao
import rahulstech.android.budgetapp.budgetdb.dao.ExpenseDao
import rahulstech.android.budgetapp.budgetdb.entity.BudgetCategoryEntity
import rahulstech.android.budgetapp.budgetdb.entity.BudgetEntity
import rahulstech.android.budgetapp.budgetdb.entity.ExpenseEntity

interface IBudgetDB {

    suspend fun <T> runInTransaction(queries: suspend ()-> T): T

    val budgetDao: BudgetDao

    val budgetCategoryDao: BudgetCategoryDao

    val expenseDao: ExpenseDao
}

@Database(
    version = BudgetDB.DB_VERSION,
    entities = [BudgetEntity::class, BudgetCategoryEntity::class, ExpenseEntity::class],
)
@TypeConverters(Converters::class)
abstract class BudgetDB: IBudgetDB, RoomDatabase() {

    companion object {

        internal const val DB_VERSION = 1
        internal const val DB_NAME = "budget.db3"

        fun getNewInstance(context: Context): BudgetDB =
            Room.databaseBuilder(context.applicationContext, BudgetDB::class.java, DB_NAME)
                .build()
    }

    override suspend fun <T> runInTransaction(queries: suspend () -> T): T =
        coroutineScope { withTransaction(queries) }

}