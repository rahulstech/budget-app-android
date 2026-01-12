package rahulstech.android.budgetapp.ui.screen

import android.content.Context
import androidx.annotation.StringRes
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface UIState<out T>{

    class Idle(): UIState<Nothing>

    data class Loading(val progress: Int = -1, val max: Int = -1, val extras: Any? = null): UIState<Nothing>

    data class Success<T>(val data: T): UIState<T>

    data class NotFound(val data: Any? = null): UIState<Nothing>

    data class Error(val cause: Throwable? = null): UIState<Nothing>
}

class UIAction<A,R>(
    val action: suspend (A)-> R?,
    val converter: (A,R?)-> UIState<R> = { args,result ->
        when(result) {
            null -> UIState.NotFound(args)
            else -> UIState.Success(result)
        }
    }
)
{
    private val _uiState = MutableSharedFlow<UIState<R>>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val uiState = _uiState.asSharedFlow()

    suspend fun doAction(arg: A) = coroutineScope {
        _uiState.tryEmit(UIState.Loading())
        try {
            val result = action(arg)
            val newState = converter(arg,result)
            _uiState.tryEmit(newState)
        }
        catch (cause: Throwable) {
            _uiState.tryEmit(UIState.Error(cause))
        }
    }
}

sealed interface UIText {
    data class StringResource(@StringRes val resId: Int, val args: List<Any> = emptyList()): UIText
    {
        fun resolveString(context: Context): String = context.getString(resId,*args.toTypedArray())
    }

    data class PlainString(val value: String): UIText
}

sealed interface UISideEffect {

    data class ShowSnackBar(val message: UIText): UISideEffect

    data class NavigateTo(val event: NavigationEvent): UISideEffect

    object ExitScreen: UISideEffect
}