package dev.simonvar.photodrop.presentation.home

import dev.simonvar.photodrop.arch.ActionDependencies
import dev.simonvar.photodrop.data.MediaRepository
import dev.simonvar.photodrop.data.TrashManager
import kotlinx.coroutines.CoroutineScope

class SwipeDependencies(
    override val coroutineScope: CoroutineScope,
    val repository: MediaRepository,
    val trashManager: TrashManager,
) : ActionDependencies()
