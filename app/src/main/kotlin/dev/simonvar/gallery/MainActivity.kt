package dev.simonvar.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.simonvar.gallery.ui.components.PermissionGate
import dev.simonvar.gallery.ui.navigation.GalleryNavigation
import dev.simonvar.gallery.ui.theme.GalleryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GalleryTheme {
                PermissionGate {
                    GalleryNavigation()
                }
            }
        }
    }
}
