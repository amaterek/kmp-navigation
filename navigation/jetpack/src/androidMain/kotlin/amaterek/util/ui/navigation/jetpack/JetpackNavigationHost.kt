@file:Suppress("NOTHING_TO_INLINE")

package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.DialogPropertiesProvider
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransitionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf

@Composable
fun JetpackNavigationHost(
    startDestination: ScreenDestination,
    graph: Set<GraphDestination>,
    parent: Navigator? = null,
) = JetpackNavigationHost(
    navigator = rememberJetpackNavigator(startDestination, graph, parent),
)

@Composable
fun JetpackNavigationHost(
    navigator: JetpackNavigator,
) {
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavHost(
            navController = navigator.navHostController,
            startDestination = navigator.startDestination.route,
        ) {
            navigator.graph.forEach { destinationClass ->
                addDestination(navigator, destinationClass)
            }
        }
    }
}

@Composable
fun rememberJetpackNavigator(
    startDestination: ScreenDestination,
    graph: Set<GraphDestination>,
    parent: Navigator?,
): JetpackNavigator {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    return remember { JetpackNavigator(startDestination, navController, graph, parent, coroutineScope) }
}

@Stable
private fun NavGraphBuilder.addDestination(navigator: JetpackNavigator, destinationClass: KClass<out ScreenDestination>) {
    @Suppress("UNCHECKED_CAST")
    val destinationRoute = (destinationClass as KClass<ScreenDestination>).baseRoute
    when {
        destinationClass.isSubclassOf(DialogDestination::class) -> {
//            TODO Should transition for dialogs be handled?
//            if (destinationClass.getTransitions() != NoneScreenTransition) {
//                 error("Screen transition are not supported for dialogs")
//            }
            @Suppress("UNCHECKED_CAST")
            dialog(
                route = destinationRoute,
                dialogProperties = (destinationClass as KClass<out DialogDestination>).getDialogProperties(),
                content = { navBackStackEntry ->
                    DestinationContent(
                        destinationClass = destinationClass,
                        navBackStackEntry = navBackStackEntry,
                        navigator = navigator,
                    )
                }
            )
        }
        else -> {
            val transitions = destinationClass.getTransitions()
            composable(
                route = destinationRoute,
                enterTransition = { transitions.enter },
                exitTransition = { transitions.exit },
                popEnterTransition = { transitions.popEnter },
                popExitTransition = { transitions.popExit },
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
}

@Composable
private inline fun DestinationContent(
    destinationClass: KClass<out ScreenDestination>,
    navBackStackEntry: NavBackStackEntry,
    navigator: JetpackNavigator,
) {
    val destination = remember {
        destinationClass.destination(navBackStackEntry).also {
            navigator.onDestinationCreated(navBackStackEntry.id, it)
        }
    }
    CompositionLocalProvider(
        LocalDestination provides destination,
    ) {
        destination.Content()
    }
}

@Stable
private inline fun KClass<out ScreenDestination>.destination(navBackStackEntry: NavBackStackEntry): ScreenDestination =
    objectInstance ?: (navBackStackEntry.arguments!!.getString(ArgumentsName)!!.deserializeDestination())

@Stable
private fun KClass<out DialogDestination>.getDialogProperties(): DialogProperties {
    objectInstance?.let {
        return it.dialogProperties
    }
    companionObjectInstance?.let {
        if (it is DialogPropertiesProvider) {
            return it.dialogProperties
        } else {
            dialogDestinationMustImplementDialogPropertiesInObjectError(baseRoute)
        }
    } ?: dialogDestinationMustImplementDialogPropertiesInObjectError(baseRoute)
}

@Stable
private fun KClass<out ScreenDestination>.getTransitions(): ScreenTransition {
    objectInstance?.let { return it.transition }
    companionObjectInstance?.let {
        if (it is ScreenTransitionProvider) {
            return it.transition
        } else {
            screenDestinationShouldImplementScreenTransitionInObjectWarn(baseRoute)
        }
    } ?: screenDestinationShouldImplementScreenTransitionInObjectWarn(baseRoute)
}

private fun dialogDestinationMustImplementDialogPropertiesInObjectError(route: String): Nothing =
    error("DialogDestination must implement DialogPropertiesProvider in a companion for non object destinations: route=$route")

private fun screenDestinationShouldImplementScreenTransitionInObjectWarn(route: String): Nothing =
    error("ScreenDestination must implement ScreenTransitionProvider in a companion for non object destinations: route=$route")