package dev.simonvar.photodrop.arch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class SailViewModel<S : ViewState, E : ViewEvent>(initialState: S) : ViewModel() {

    protected abstract val dependencies: ActionDependencies

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()

    @Suppress("UNCHECKED_CAST")
    protected fun <D : ActionDependencies> dispatch(
        action: UiAction<D, S, E>,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) {
        viewModelScope.launch(dispatcher) {
            val scope = ActionScope(_state, _events)
            action.execute(dependencies as D, scope)
        }
    }

    protected fun updateState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    protected fun emitEvent(event: E) {
        _events.trySend(event)
    }
}
