package rahulstech.android.budgetapp.ui.screen.viewexpenses

import rahulstech.android.budgetapp.repository.model.Expense
import rahulstech.android.budgetapp.ui.components.ExpenseDialogState

sealed interface ViewExpenseUIEvent {

    data class ShowExpenseOptionsDialogEvent(val expense: Expense): ViewExpenseUIEvent

    data class ShowEditExpenseDialogEvent(val expense: Expense): ViewExpenseUIEvent

    data class ShowDeleteExpenseDialogEvent(val expense: Expense): ViewExpenseUIEvent
}

typealias ViewExpenseUIEventCallback = (ViewExpenseUIEvent)-> Unit

data class ViewExpenseUIState (
    val addExpenseDialog: ExpenseDialogState = ExpenseDialogState(),
    val editExpenseDialog: ExpenseDialogState = ExpenseDialogState(),
    val deleteExpenseDialog: ExpenseDialogState = ExpenseDialogState(),
    val expenseOptionsDialog: ExpenseDialogState = ExpenseDialogState(),
)