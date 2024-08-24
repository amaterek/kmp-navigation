package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.NavigationDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.stack.StackEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import cafe.adriel.voyager.navigator.Navigator as VNavigator

@OptIn(InternalNavigation::class)
@NonRestartableComposable
@Composable
fun VoyagerNavigationHost(
    startBackStack: List<ScreenDestination>,
    graph: Set<GraphDestination>,
    parent: Navigator? = null,
) = VoyagerNavigationHost(
    navigator = rememberVoyagerNavigator(startBackStack, graph, parent),
)

@OptIn(InternalNavigation::class)
@NonRestartableComposable
@Composable
fun VoyagerNavigationHost(
    startDestination: ScreenDestination,
    graph: Set<GraphDestination>,
    parent: Navigator? = null,
) = VoyagerNavigationHost(
    navigator = rememberVoyagerNavigator(listOf(startDestination), graph, parent),
)

@OptIn(InternalNavigation::class, InternalVoyagerApi::class)
@Composable
fun VoyagerNavigationHost(
    navigator: Navigator,
) {
    navigator as VoyagerNavigator
    CompositionLocalProvider(
        LocalNavigator provides navigator,
    ) {
        VNavigator(
            screens = navigator.startBackStack.map { VoyagerBackStackEntry(it) },
            onBackPressed = { false },
        ) { vNavigator ->
            navigator.navHostController = vNavigator
            LaunchedEffect(vNavigator.key) {
                snapshotFlow { (vNavigator.lastItem as VoyagerBackStackEntry).destination }
                    .onEach { navigator.currentDestinationFlow.value = it }
                    .launchIn(this)
            }

            val screenItem by remember {
                derivedStateOf {
                    vNavigator.items.last { (it as VoyagerBackStackEntry).destination !is DialogDestination } as VoyagerBackStackEntry
                }
            }

            val dialogItem by remember {
                derivedStateOf {
                    vNavigator.items.lastOrNull { (it as VoyagerBackStackEntry).destination is DialogDestination } as? VoyagerBackStackEntry
                }
            }

            ScreenItem(vNavigator, screenItem)

            dialogItem?.let {
                NavigationDialog(
                    dialogDestination = it.destination as DialogDestination,
                    onDismissRequest = { navigator.navigateTo(PreviousDestination) },
                    content = { vNavigator.ScreenContent(it) },
                )
            }
        }
    }
}

@OptIn(InternalNavigation::class)
@NonRestartableComposable
@Composable
fun rememberVoyagerNavigator(
    startDestination: ScreenDestination,
    graph: Set<GraphDestination>,
    parent: Navigator?,
): Navigator = remember { VoyagerNavigator(listOf(startDestination), graph, parent) }

@OptIn(InternalNavigation::class)
@NonRestartableComposable
@Composable
fun rememberVoyagerNavigator(
    startBackStack: List<ScreenDestination>,
    graph: Set<GraphDestination>,
    parent: Navigator?,
): Navigator = remember { VoyagerNavigator(startBackStack, graph, parent) }

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ScreenItem(vNavigator: VNavigator, screen: VoyagerBackStackEntry) {
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = screen,
        transitionSpec = {
            val item = targetState
            val lastItem = initialState
            val transition = item.destination.transition
            val isPop = vNavigator.lastEvent == StackEvent.Pop
            val lastTransition = if (item != lastItem) lastItem.destination.transition else null
            // Behaviour compatible with JetPack navigation
            val (enterTransition, exitTransition) = if (isPop) {
                lastTransition?.let {
                    transition.popEnter to it.popExit
                } ?: run {
                    transition.popEnter to transition.popExit
                }
            } else {
                lastTransition?.let {
                    transition.enter to it.exit
                } ?: run {
                    transition.enter to transition.exit
                }
            }
            enterTransition togetherWith exitTransition
        },
        contentKey = { it.key }
    ) { currentItem ->
        val isTransitionRunning = transition.isRunning

        // TODO Update destination's lifecycle state

        vNavigator.ScreenContent(currentItem)
        if (isTransitionRunning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onKeyEvent { true }
                    .pointerInput(Unit) { },
            )
        }
    }
}

@Composable
private fun VNavigator.ScreenContent(screen: VoyagerBackStackEntry) {
    CompositionLocalProvider(
        LocalDestination provides screen.destination,
    ) {
        saveableState("currentScreen", screen) {
            screen.Content()
        }
    }
}
