package dev.simonvar.photodrop.presentation.home

import dev.simonvar.photodrop.arch.ActionDependencies
import dev.simonvar.photodrop.data.media.MediaRepository
import dev.simonvar.photodrop.data.trash.TrashRepository
import kotlinx.coroutines.CoroutineScope

class SwipeDependencies(
    override val coroutineScope: CoroutineScope,
    val repository: MediaRepository,
    val trashRepository: TrashRepository,
) : ActionDependencies()
