package dev.simonvar.photodrop.arch

import kotlinx.coroutines.CoroutineScope

interface ViewState

interface ViewEvent

abstract class ActionDependencies {
    abstract val coroutineScope: CoroutineScope
}
