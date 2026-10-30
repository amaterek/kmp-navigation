@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(InternalNavigation::class)

package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigationResultHandler
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.NavigationGraph
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlin.reflect.KClass

@Composable
fun JetpackNavigationHost(
    startDestination: ScreenDestination,
    graph: NavigationGraph,
    parent: Navigator? = null,
    defaultTransition: ScreenTransition = FadeScreenTransition,
) = JetpackNavigationHost(
    navigator = rememberJetpackNavigator(startDestination, graph, parent, defaultTransition) as JetpackNavigator,
)

@Composable
fun JetpackNavigationHost(
    navigator: Navigator,
) {
    navigator as JetpackNavigator
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavHost(
            navController = navigator.navHostController,
            startDestination = navigator.startDestination.route,
        ) {
            navigator.graph.entries.forEach { (destinationClass, entry) ->
                addDestination(navigator, destinationClass, entry)
            }
        }
    }
}

@Composable
fun rememberJetpackNavigator(
    startDestination: ScreenDestination,
    graph: NavigationGraph,
    parent: Navigator?,
    defaultTransition: ScreenTransition = FadeScreenTransition,
): Navigator {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    return remember { JetpackNavigator(startDestination, navController, graph, coroutineScope, parent, defaultTransition) }
}

@Stable
private fun NavGraphBuilder.addDestination(
    navigator: JetpackNavigator,
    destinationClass: KClass<out ScreenDestination>,
    entry: NavigationGraph.Entry,
) {
    @Suppress("UNCHECKED_CAST")
    val destinationRoute = (destinationClass as KClass<ScreenDestination>).baseRoute
    val dialogProperties = entry.dialogProperties
    if (dialogProperties != null) {
        dialog(
            route = destinationRoute,
            dialogProperties = dialogProperties,
            content = { navBackStackEntry ->
                DestinationContent(
                    destinationClass = destinationClass,
                    navBackStackEntry = navBackStackEntry,
                    navigator = navigator,
                )
            }
        )
    } else {
        composable(
            route = destinationRoute,
            enterTransition = { navigator.transitionOf(targetState, destinationClass).enter },
            exitTransition = { navigator.transitionOf(initialState, destinationClass).exit },
            popEnterTransition = { navigator.transitionOf(targetState, destinationClass).popEnter },
            popExitTransition = { navigator.transitionOf(initialState, destinationClass).popExit },
            content = { navBackStackEntry ->
                DestinationContent(
                    destinationClass = destinationClass,
                    navBackStackEntry = navBackStackEntry,
                    navigator = navigator,
                )
            }
        )
    }
}

@OptIn(InternalNavigation::class)
@Composable
private inline fun DestinationContent(
    destinationClass: KClass<out ScreenDestination>,
    navBackStackEntry: NavBackStackEntry,
    navigator: JetpackNavigator,
) {
    val destination = remember { navigator.getDestination(navBackStackEntry, destinationClass) }
    CompositionLocalProvider(
        LocalDestination provides destination,
        LocalNavigationResultHandler provides navBackStackEntry,
    ) {
        destination.Content()
    }
}

@Stable
internal inline fun KClass<out ScreenDestination>.destination(navBackStackEntry: NavBackStackEntry): ScreenDestination =
    objectInstance ?: navBackStackEntry.arguments!!.getString(ArgumentsName)!!.deserializeDestination()
