package dev.simonvar.gallery.arch

import kotlinx.coroutines.CoroutineScope

interface ViewState

interface ViewEvent

abstract class ActionDependencies {
    abstract val coroutineScope: CoroutineScope
}
