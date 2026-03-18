package dev.simonvar.gallery.ui.swipe

import dev.simonvar.gallery.arch.ActionDependencies
import dev.simonvar.gallery.data.MediaRepository
import dev.simonvar.gallery.data.TrashManager
import kotlinx.coroutines.CoroutineScope

class SwipeDependencies(
    override val coroutineScope: CoroutineScope,
    val repository: MediaRepository,
    val trashManager: TrashManager,
) : ActionDependencies()
