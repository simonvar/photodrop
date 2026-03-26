package dev.simonvar.photodrop

import android.app.ComponentCaller
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import dev.simonvar.photodrop.data.trash.LocalTrashRepository
import dev.simonvar.photodrop.ui.block.PermissionGate
import dev.simonvar.photodrop.presentation.main.MainNode
import dev.simonvar.photodrop.ui.theme.GalleryTheme

class PhotodropActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appDependencies = (application as PhotodropApplication).dependencies

        setContent {
            CompositionLocalProvider(
                LocalTrashRepository provides appDependencies.trashRepository,
            ) {
                GalleryTheme {
                    PermissionGate {
                        MainNode()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}
