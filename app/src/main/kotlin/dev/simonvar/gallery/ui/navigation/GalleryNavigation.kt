package dev.simonvar.gallery.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.simonvar.gallery.ui.fullscreen.FullscreenScreen
import dev.simonvar.gallery.ui.swipe.SwipeScreen
import dev.simonvar.gallery.ui.trash.TrashScreen

@Composable
fun GalleryNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "swipe",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable("swipe") {
            SwipeScreen(
                onNavigateToTrash = { navController.navigate("trash") },
                onNavigateToFullscreen = { itemId ->
                    navController.navigate("fullscreen/$itemId")
                },
            )
        }
        composable("trash") {
            TrashScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "fullscreen/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
        ) {
            FullscreenScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
