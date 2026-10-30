@file:OptIn(InternalNavigation::class)

package amaterek.util.ui.navigation.nav3

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigationResultHandler
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.NavigationGraph
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@Composable
fun Nav3NavigationHost(
    startDestination: ScreenDestination,
    graph: NavigationGraph,
    parent: Navigator? = null,
    defaultTransition: ScreenTransition = FadeScreenTransition,
) = Nav3NavigationHost(
    navigator = rememberNav3Navigator(startDestination, graph, parent, defaultTransition),
)

/**
 * @param isPredictiveBackEnabled when `false`, a back gesture doesn't show the previous destination while it's in
 * progress (the destination is popped when the gesture completes), e.g. for tabs that aren't a real back stack.
 */
@Composable
fun Nav3NavigationHost(
    navigator: Navigator,
    isPredictiveBackEnabled: Boolean = true,
) {
    navigator as Nav3Navigator
    val sceneStrategies = remember { listOf(DialogSceneStrategy<Nav3Entry>(), SinglePaneSceneStrategy()) }
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = navigator.entries,
            onBack = { navigator.navigateTo(PreviousDestination) },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            sceneStrategies = sceneStrategies,
            transitionSpec = {
                navigator.transitionOf(targetState).enter togetherWith navigator.transitionOf(initialState).exit
            },
            popTransitionSpec = { navigator.popTransitionOf(initialState, targetState) },
            predictivePopTransitionSpec = { navigator.popTransitionOf(initialState, targetState) },
            entryProvider = navigator::navEntryOf,
        )
        if (!isPredictiveBackEnabled) {
            // Registered after the NavDisplay's handler, so it receives the gesture instead and the NavDisplay never
            // starts its predictive back transition
            NavigationBackHandler(
                state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
                isBackEnabled = navigator.entries.size > 1,
                onBackCompleted = { navigator.navigateTo(PreviousDestination) },
            )
        }
    }
}

@Composable
fun rememberNav3Navigator(
    startDestination: ScreenDestination,
    graph: NavigationGraph,
    parent: Navigator?,
    defaultTransition: ScreenTransition = FadeScreenTransition,
): Navigator {
    val entries = rememberSaveable(saver = remember { nav3EntriesSaver(graph) }) {
        listOf(Nav3Entry(startDestination)).toMutableStateList()
    }
    // Not saved, like the results of the other navigators: results don't have to be serializable
    val results = remember { mutableStateMapOf<String, Any>() }
    return remember { Nav3Navigator(graph, entries, results, parent, defaultTransition) }
}

private fun Nav3Navigator.navEntryOf(entry: Nav3Entry): NavEntry<Nav3Entry> {
    val dialogProperties = graph.entries[entry.destination::class]?.dialogProperties
    val metadata = if (dialogProperties != null) {
        DialogSceneStrategy.dialog(dialogProperties)
    } else {
        metadata { put(ScreenTransitionKey, entry.destination.transition ?: defaultTransition) }
    }
    return NavEntry(key = entry, contentKey = entry.id, metadata = metadata) { DestinationContent(it) }
}

private fun Nav3Navigator.popTransitionOf(initialScene: Scene<Nav3Entry>, targetScene: Scene<Nav3Entry>): ContentTransform =
    transitionOf(targetScene).popEnter togetherWith transitionOf(initialScene).popExit

private fun Nav3Navigator.transitionOf(scene: Scene<Nav3Entry>): ScreenTransition =
    scene.metadata[ScreenTransitionKey] ?: defaultTransition

@Composable
private fun DestinationContent(entry: Nav3Entry) {
    CompositionLocalProvider(
        LocalDestination provides entry.destination,
        LocalNavigationResultHandler provides entry.id,
    ) {
        entry.destination.Content()
    }
}

private object ScreenTransitionKey : NavMetadataKey<ScreenTransition>
