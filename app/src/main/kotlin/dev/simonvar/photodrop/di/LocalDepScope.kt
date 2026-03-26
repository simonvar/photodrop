package dev.simonvar.photodrop.di

import androidx.compose.runtime.staticCompositionLocalOf
import dev.simonvar.photodrop.ApplicationDepScope

val LocalDepScope = staticCompositionLocalOf<ApplicationDepScope> {
    error("No DependencyScope provided")
}
