package rahulstech.android.budgetapp.ui.screen

sealed class UIState<out T>{

    class Idle(): UIState<Nothing>()

    data class Loading(val progress: Int = -1, val max: Int = -1, val extras: Map<String,Any?> = emptyMap()): UIState<Nothing>()

    data class Success<T>(val data: T): UIState<T>()

    data class NotFound<T>(val what: T): UIState<T>()

    data class Error(val cause: Throwable? = null): UIState<Nothing>()
}