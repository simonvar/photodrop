package dev.simonvar.photodrop

import android.app.Application
import dev.simonvar.photodrop.data.media.MediaDepMod
import dev.simonvar.photodrop.data.media.MediaDepModImpl
import dev.simonvar.photodrop.data.trash.TrashDepMod
import dev.simonvar.photodrop.data.trash.TrashDepModImpl

class ApplicationDepScope(val app: Application) :
    MediaDepMod by MediaDepModImpl(app),
    TrashDepMod by TrashDepModImpl()
