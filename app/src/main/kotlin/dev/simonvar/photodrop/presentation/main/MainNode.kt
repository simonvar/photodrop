package dev.simonvar.photodrop.presentation.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.simonvar.photodrop.presentation.favorites.Favorites
import dev.simonvar.photodrop.presentation.favorites.FavoritesNode
import dev.simonvar.photodrop.presentation.home.HomeNode
import dev.simonvar.photodrop.presentation.home.Home
import dev.simonvar.photodrop.presentation.trash.TrashNode
import dev.simonvar.photodrop.presentation.trash.Trash

@Composable
fun MainNode() {
    val navBackStack = rememberNavBackStack(Home)
    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        onBack = { navBackStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        transitionSpec = { slideInHorizontally { it } togetherWith ExitTransition.KeepUntilTransitionsFinished },
        popTransitionSpec = { EnterTransition.None togetherWith slideOutHorizontally { it } },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith slideOutHorizontally { it } },
        entryProvider = entryProvider {
            entry<Home> {
                HomeNode(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToTrash = { navBackStack.add(Trash) },
                    onNavigateToFavorites = { navBackStack.add(Favorites) },
                )
            }
            entry<Trash> {
                TrashNode(
                    modifier = Modifier.fillMaxSize(),
                    onBack = { navBackStack.removeLastOrNull() }
                )
            }
            entry<Favorites> {
                FavoritesNode(
                    modifier = Modifier.fillMaxSize(),
                    onBack = { navBackStack.removeLastOrNull() }
                )
            }
        }
    )
}
