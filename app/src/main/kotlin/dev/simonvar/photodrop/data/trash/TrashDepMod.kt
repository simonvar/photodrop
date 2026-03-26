package dev.simonvar.photodrop.data.trash

interface TrashDepMod {
    val trashRepository: TrashRepository
}

class TrashDepModImpl : TrashDepMod {
    override val trashRepository: TrashRepository by lazy { TrashRepositoryImpl() }
}
