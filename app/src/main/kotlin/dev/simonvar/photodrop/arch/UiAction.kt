package dev.simonvar.photodrop.arch

interface UiAction<D : ActionDependencies, S : ViewState, E : ViewEvent> {
    suspend fun execute(dependencies: D, scope: ActionScope<S, E>)
}
