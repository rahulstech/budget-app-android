package rahulstech.android.budgetapp.budgetdb

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import rahulstech.android.budgetapp.budgetdb.fakedata.FakeDataV1
import java.time.LocalDate

fun createInMemoryBudgetDB(callback: RoomDatabase.Callback? = null): BudgetDB {
    val content = ApplicationProvider.getApplicationContext<Application>()
    val builder = Room.inMemoryDatabaseBuilder(content, BudgetDB::class.java)
        .allowMainThreadQueries()
    callback?.let { builder.addCallback(it) }
    return builder.build()
}

fun fakeData(dbVersion: Int = BudgetDB.DB_VERSION): RoomDatabase.Callback? =
    when(dbVersion) {
        1 -> FakeDataV1()
        else -> null
    }

fun epochPlusDays(days: Int): Long =
    Converters.localDateToLong(
        LocalDate.of(1970,1,1).plusDays(days.toLong())
    )!!