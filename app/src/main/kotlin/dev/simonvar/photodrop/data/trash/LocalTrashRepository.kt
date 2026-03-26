package dev.simonvar.photodrop.data.trash

import androidx.compose.runtime.staticCompositionLocalOf

val LocalTrashRepository = staticCompositionLocalOf<TrashRepository> {
    error("No TrashRepository provided")
}
