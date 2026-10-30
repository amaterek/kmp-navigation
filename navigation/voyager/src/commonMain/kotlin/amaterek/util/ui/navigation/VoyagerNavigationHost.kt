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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.navigator.Navigator as VNavigator

@NonRestartableComposable
@Composable
fun VoyagerNavigationHost(
    startBackStack: List<ScreenDestination>,
    graph: NavigationGraph,
    parent: Navigator? = null,
    defaultTransition: ScreenTransition = FadeScreenTransition,
) = VoyagerNavigationHost(
    navigator = rememberVoyagerNavigator(startBackStack, graph, parent, defaultTransition),
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
    val hostLifecycleOwner = LocalLifecycleOwner.current
    val viewModelStoreProvider = rememberViewModelStoreProvider()
    val entryOwners = remember(hostLifecycleOwner, viewModelStoreProvider) {
        VoyagerEntryOwners(hostLifecycleOwner, viewModelStoreProvider)
    }
    DisposableEffect(entryOwners) {
        onDispose { entryOwners.disposeLifecycles() }
    }
    CompositionLocalProvider(
        LocalNavigator provides navigator,
    ) {
        VNavigator(
            screens = remember(navigator) { navigator.startBackStack.map { navigator.backStackEntryOf(it) } },
            // Voyager disposes the removed screens only after a pop or replace, not when a push follows them
            disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false),
            onBackPressed = null,
        ) { vNavigator ->
            // In the composition, so the screens can navigate in their first composition
            remember(vNavigator) { navigator.attach(vNavigator) }

            DisposeRemovedEntries(vNavigator, entryOwners)

            // Voyager handles back only on Android. Registered before the screens, so their handlers and the ones
            // of nested hosts take precedence.
            NavigationBackHandler(
                state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
                isBackEnabled = vNavigator.canPop,
                onBackCompleted = { navigator.navigateTo(PreviousDestination) },
            )

            val screenItem by remember {
                derivedStateOf {
                    vNavigator.items.last {
                        navigator.dialogPropertiesOf(navigator.destinationOf(it as VoyagerBackStackEntry)) == null
                    } as VoyagerBackStackEntry
                }
            }

            val dialogItems by remember {
                derivedStateOf {
                    vNavigator.items.takeLastWhile {
                        navigator.dialogPropertiesOf(navigator.destinationOf(it as VoyagerBackStackEntry)) != null
                    }.map { it as VoyagerBackStackEntry }
                }
            }

            // Like Jetpack navigation, all dialogs above the screen are shown and everything below the top one is
            // paused
            ScreenItem(vNavigator, navigator, entryOwners, screenItem, isBelowDialog = dialogItems.isNotEmpty())
            DialogItems(vNavigator, navigator, entryOwners, dialogItems)
        }
    }
}

@InternalNavigation
@Composable
private fun DialogItems(
    vNavigator: VNavigator,
    navigator: VoyagerNavigator,
    entryOwners: VoyagerEntryOwners,
    dialogItems: List<VoyagerBackStackEntry>,
) {
    dialogItems.forEach { dialogItem ->
        key(dialogItem) {
            val maxLifecycle =
                if (dialogItem === dialogItems.last()) Lifecycle.State.RESUMED else Lifecycle.State.STARTED
            Dialog(
                onDismissRequest = { navigator.navigateTo(PreviousDestination) },
                properties = navigator.dialogPropertiesOf(navigator.destinationOf(dialogItem)) ?: DialogProperties(),
            ) {
                vNavigator.ScreenContent(navigator, entryOwners, dialogItem, maxLifecycle)
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

@OptIn(InternalVoyagerApi::class)
@Composable
private fun DisposeRemovedEntries(vNavigator: VNavigator, entryOwners: VoyagerEntryOwners) {
    val entries = vNavigator.items
    DisposableEffect(entries) {
        onDispose {
            val newEntries = vNavigator.items
            entries
                .filter { entry -> newEntries.none { it === entry } }
                .forEach {
                    vNavigator.dispose(it)
                    entryOwners.dispose(it as VoyagerBackStackEntry)
                }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@InternalNavigation
@Composable
private fun ScreenItem(
    vNavigator: VNavigator,
    navigator: VoyagerNavigator,
    entryOwners: VoyagerEntryOwners,
    screen: VoyagerBackStackEntry,
    isBelowDialog: Boolean,
) {
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

        // The screen leaving with the transition is paused, like below a dialog
        val maxLifecycle =
            if (currentItem === screen && !isBelowDialog) Lifecycle.State.RESUMED else Lifecycle.State.STARTED
        vNavigator.ScreenContent(navigator, entryOwners, currentItem, maxLifecycle)
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
private fun VNavigator.ScreenContent(
    navigator: VoyagerNavigator,
    entryOwners: VoyagerEntryOwners,
    screen: VoyagerBackStackEntry,
    maxLifecycle: Lifecycle.State,
) {
    CompositionLocalProvider(
        LocalDestination provides navigator.destinationOf(screen),
        LocalNavigationResultHandler provides screen,
    ) {
        saveableState("currentScreen", screen) {
            val lifecycleOwner = entryOwners.lifecycleOwnerOf(screen)
            SideEffect { lifecycleOwner.maxState = maxLifecycle }
            DisposableEffect(lifecycleOwner) {
                onDispose { lifecycleOwner.maxState = Lifecycle.State.CREATED }
            }
            // The saved state registry of the screen's saveable state, so view models can use SavedStateHandle
            val viewModelStoreOwner = rememberViewModelStoreOwner(screen.key, entryOwners.viewModelStoreProvider)
            // Inside Voyager's saveable state, which provides the lifecycle and view model store of its Android owner
            CompositionLocalProvider(
                LocalLifecycleOwner provides lifecycleOwner,
                LocalViewModelStoreOwner provides viewModelStoreOwner,
            ) {
                screen.Content()
            }
        }
    }
}
