package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.stack.StackEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import cafe.adriel.voyager.navigator.Navigator as VNavigator

@NonRestartableComposable
@Composable
fun VoyagerNavigationHost(
    startBackStack: List<ScreenDestination>,
    graph: NavigationGraph,
    parent: Navigator? = null,
) = VoyagerNavigationHost(
    navigator = rememberVoyagerNavigator(startBackStack, graph, parent),
)

@NonRestartableComposable
@Composable
fun VoyagerNavigationHost(
    startDestination: ScreenDestination,
    graph: NavigationGraph,
    parent: Navigator? = null,
    defaultTransition: ScreenTransition = FadeScreenTransition,
) = VoyagerNavigationHost(
    navigator = rememberVoyagerNavigator(listOf(startDestination), graph, parent, defaultTransition),
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
            screens = navigator.startBackStack.map { navigator.backStackEntryOf(it) },
            onBackPressed = null,
        ) { vNavigator ->
            navigator.navHostController = vNavigator

            // Voyager handles back only on Android. Registered before the screens, so their handlers and the ones
            // of nested hosts take precedence.
            NavigationBackHandler(
                state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
                isBackEnabled = vNavigator.canPop,
                onBackCompleted = { navigator.navigateTo(PreviousDestination) },
            )

            LaunchedEffect(vNavigator.key) {
                snapshotFlow { navigator.destinationOf(vNavigator.lastItem as VoyagerBackStackEntry) }
                    .onEach { navigator.currentDestinationFlow.value = it }
                    .launchIn(this)
            }

            val screenItem by remember {
                derivedStateOf {
                    vNavigator.items.last {
                        navigator.dialogPropertiesOf(navigator.destinationOf(it as VoyagerBackStackEntry)) == null
                    } as VoyagerBackStackEntry
                }
            }

            val dialogItem by remember {
                derivedStateOf {
                    vNavigator.items.lastOrNull {
                        navigator.dialogPropertiesOf(navigator.destinationOf(it as VoyagerBackStackEntry)) != null
                    } as? VoyagerBackStackEntry
                }
            }

            ScreenItem(vNavigator, navigator, screenItem)

            dialogItem?.let {
                Dialog(
                    onDismissRequest = { navigator.navigateTo(PreviousDestination) },
                    properties = navigator.dialogPropertiesOf(navigator.destinationOf(it)) ?: DialogProperties(),
                    content = { vNavigator.ScreenContent(navigator, it) },
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
    graph: NavigationGraph,
    parent: Navigator?,
    defaultTransition: ScreenTransition = FadeScreenTransition,
): Navigator = remember { VoyagerNavigator(listOf(startDestination), graph, parent, defaultTransition) }

@OptIn(InternalNavigation::class)
@NonRestartableComposable
@Composable
fun rememberVoyagerNavigator(
    startBackStack: List<ScreenDestination>,
    graph: NavigationGraph,
    parent: Navigator?,
    defaultTransition: ScreenTransition = FadeScreenTransition,
): Navigator = remember { VoyagerNavigator(startBackStack, graph, parent, defaultTransition) }

@OptIn(ExperimentalAnimationApi::class)
@InternalNavigation
@Composable
private fun ScreenItem(vNavigator: VNavigator, navigator: VoyagerNavigator, screen: VoyagerBackStackEntry) {
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = screen,
        transitionSpec = {
            val item = targetState
            val lastItem = initialState
            val transition = navigator.destinationOf(item).transition ?: navigator.defaultTransition
            val isPop = vNavigator.lastEvent == StackEvent.Pop
            val lastTransition = if (item != lastItem) {
                navigator.destinationOf(lastItem).transition ?: navigator.defaultTransition
            } else null
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

        vNavigator.ScreenContent(navigator, currentItem)
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

@InternalNavigation
@Composable
private fun VNavigator.ScreenContent(navigator: VoyagerNavigator, screen: VoyagerBackStackEntry) {
    CompositionLocalProvider(
        LocalDestination provides navigator.destinationOf(screen),
        LocalNavigationResultHandler provides screen,
    ) {
        saveableState("currentScreen", screen) {
            screen.Content()
        }
    }
}
