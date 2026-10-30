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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun JetpackNavigationHost(
    startDestination: ScreenDestination,
    graph: NavigationGraph,
    parent: Navigator? = null,
    defaultTransition: ScreenTransition = FadeScreenTransition,
) = JetpackNavigationHost(
    navigator = rememberJetpackNavigator(startDestination, graph, parent, defaultTransition),
)

@Composable
fun JetpackNavigationHost(
    navigator: Navigator,
) {
    navigator as JetpackNavigator
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavHost(
            navController = navigator.navHostController,
            startDestination = navigator.startEntry.route,
        ) {
            navigator.graph.entries.values.forEach { entry ->
                addDestination(navigator, entry)
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
    return remember {
        JetpackNavigator(startDestination, navController, graph, coroutineScope, parent, defaultTransition)
    }
}

private fun NavGraphBuilder.addDestination(navigator: JetpackNavigator, entry: NavigationGraph.Entry) {
    val arguments = listOf(
        navArgument(ArgumentsName) {
            type = NavType.StringType
            nullable = true
            // Start destination is navigated by route pattern, so its arguments are passed as default value
            defaultValue = if (entry == navigator.startEntry) navigator.startDestinationArgument else null
        },
    )
    val dialogProperties = entry.dialogProperties
    if (dialogProperties != null) {
        dialog(
            route = entry.route,
            arguments = arguments,
            dialogProperties = dialogProperties,
            content = { navBackStackEntry -> DestinationContent(navigator, navBackStackEntry) },
        )
    } else {
        composable(
            route = entry.route,
            arguments = arguments,
            enterTransition = { navigator.transitionOf(targetState).enter },
            exitTransition = { navigator.transitionOf(initialState).exit },
            popEnterTransition = { navigator.transitionOf(targetState).popEnter },
            popExitTransition = { navigator.transitionOf(initialState).popExit },
            content = { navBackStackEntry -> DestinationContent(navigator, navBackStackEntry) },
        )
    }
}

@Composable
private fun DestinationContent(
    navigator: JetpackNavigator,
    navBackStackEntry: NavBackStackEntry,
) {
    val destination = remember(navBackStackEntry) { navigator.getDestination(navBackStackEntry) }
    CompositionLocalProvider(
        LocalDestination provides destination,
        LocalNavigationResultHandler provides navBackStackEntry,
    ) {
        destination.Content()
    }
}
