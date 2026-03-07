package dev.simonvar.gallery.arch

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

open class ActionScope<S : ViewState, E : ViewEvent>(
    private val stateFlow: MutableStateFlow<S>,
    private val eventChannel: Channel<E>,
) {
    val currentState: S get() = stateFlow.value

    fun setState(reducer: S.() -> S) {
        stateFlow.update(reducer)
    }

    fun sendEvent(event: E) {
        eventChannel.trySend(event)
    }
}
