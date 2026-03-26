package dev.simonvar.photodrop

import android.app.Application
import dev.simonvar.photodrop.data.trash.TrashDepMod
import dev.simonvar.photodrop.data.trash.TrashDepModImpl

class ApplicationDepScope(val app: Application) :
    TrashDepMod by TrashDepModImpl()
