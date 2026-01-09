package rahulstech.android.budgetapp.ui.screen.viewexpenses

import rahulstech.android.budgetapp.repository.model.Expense
import java.time.LocalDate

sealed interface ExpenseListItem {

    data class ItemHeader(val date: LocalDate): ExpenseListItem

    data class ItemExpense(
        val value: Expense
    ): ExpenseListItem


}