package rahulstech.android.budgetapp.repository

import androidx.paging.PagingSource
import rahulstech.android.budgetapp.budgetdb.dao.ExpenseDao
import rahulstech.android.budgetapp.budgetdb.model.ExpenseWithCategoryModel
import java.time.LocalDate

data class ExpenseFilterParams(
    val budgetId: Long,
    val categories: List<Long> = emptyList(),
    val dateRange: Pair<LocalDate, LocalDate>? = null,
    val newestFirst: Boolean = true
) {

    internal fun createPageSource(dao: ExpenseDao): PagingSource<Int, ExpenseWithCategoryModel> =
        dao.observeExpenses(budgetId, categories, dateRange, newestFirst)
}
