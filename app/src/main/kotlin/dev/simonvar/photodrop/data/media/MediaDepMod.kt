package dev.simonvar.photodrop.data.media

import android.app.Application

interface MediaDepMod {
    val mediaRepository: MediaRepository
}

class MediaDepModImpl(app: Application) : MediaDepMod {
    override val mediaRepository: MediaRepository by lazy { MediaRepositoryImpl(app) }
}
