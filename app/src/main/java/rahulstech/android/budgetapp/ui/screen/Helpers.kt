package rahulstech.android.budgetapp.ui.screen

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class UIState<out T>{

    class Idle(): UIState<Nothing>()

    data class Loading(val progress: Int = -1, val max: Int = -1, val extras: Map<String,Any?> = emptyMap()): UIState<Nothing>()

    data class Success<T>(val data: T): UIState<T>()

    class NotFound(): UIState<Nothing>()

    data class Error(val cause: Throwable? = null): UIState<Nothing>()
}

class UIAction<A,R>(
    val action: suspend (A)-> R?,
    val converter: (A,R?)-> UIState<R> = { _,result ->
        when(result) {
            null -> UIState.NotFound()
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