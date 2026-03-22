package dev.simonvar.gallery.presentation.main

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
import dev.simonvar.gallery.presentation.home.HomeNode
import dev.simonvar.gallery.presentation.home.api.Home
import dev.simonvar.gallery.presentation.trash.TrashNode
import dev.simonvar.gallery.presentation.trash.api.Trash

@Composable
fun MainNode() {
    val navBackStack = rememberNavBackStack(Home)
    NavDisplay(
        modifier = Modifier.fillMaxSize(),
        backStack = navBackStack,
        onBack = { navBackStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        transitionSpec = {
            slideInHorizontally { it } togetherWith ExitTransition.KeepUntilTransitionsFinished
        },
        popTransitionSpec = {
            EnterTransition.None togetherWith slideOutHorizontally { it }
        },
        predictivePopTransitionSpec = {
            EnterTransition.None togetherWith slideOutHorizontally { it }
        },
        entryProvider = entryProvider {
            entry<Home> {
                HomeNode(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToTrash = { navBackStack.add(Trash) },
                )
            }
            entry<Trash> {
                TrashNode(
                    modifier = Modifier.fillMaxSize(),
                    onBack = { navBackStack.removeLastOrNull() }
                )
            }
        }
    )
}
