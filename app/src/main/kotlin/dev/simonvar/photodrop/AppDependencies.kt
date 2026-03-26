package dev.simonvar.photodrop

import dev.simonvar.photodrop.data.trash.TrashRepository
import dev.simonvar.photodrop.data.trash.TrashRepositoryImpl

class AppDependencies {

    val trashRepository: TrashRepository by lazy { TrashRepositoryImpl() }
}
