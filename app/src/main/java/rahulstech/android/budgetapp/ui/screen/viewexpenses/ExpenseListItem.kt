package rahulstech.android.budgetapp.ui.screen.viewexpenses

import rahulstech.android.budgetapp.repository.model.Expense
import java.time.LocalDate

sealed interface ExpenseListItem {

    val key: Any

    data class ItemHeader(val date: LocalDate): ExpenseListItem {
        override val key: Any = date.toEpochDay()
    }

    data class ItemExpense(val value: Expense): ExpenseListItem {
        override val key: Any = value.id
    }
}