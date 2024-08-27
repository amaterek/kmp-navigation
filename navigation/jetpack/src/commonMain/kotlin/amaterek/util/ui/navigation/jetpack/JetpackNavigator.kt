@file:Suppress("NOTHING_TO_INLINE")

package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.BaseNavigator
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

@InternalNavigation
class JetpackNavigator(
    internal val startDestination: ScreenDestination,
    internal val navHostController: NavHostController,
    internal val graph: Set<GraphDestination>,
    private val coroutineScope: CoroutineScope,
    parent: Navigator?,
    internal val defaultTransition: ScreenTransition,
) : BaseNavigator(graph, parent) {

    private val screenDestinationsMap = mutableMapOf<String, ScreenDestination>()

    internal fun getDestination(navBackStackEntry: NavBackStackEntry, destinationClass: KClass<out ScreenDestination>): ScreenDestination {
        var destination = screenDestinationsMap[navBackStackEntry.id]
        if (destination == null) {
            destination = destinationClass.destination(navBackStackEntry)
            screenDestinationsMap[navBackStackEntry.id] = destination
        }
        return destination
    }

    private inner class JetpackBackStack : Navigator.BackStack {

        private val _currentEntryFlow = MutableStateFlow(startDestination)

        init {
            navHostController.currentBackStackEntryFlow
                .onEach { navBackStackEntry ->
                    var currentDestination: ScreenDestination? = screenDestinationsMap[navBackStackEntry.id]
                    if (currentDestination == null) {
                        val destinationClass = navBackStackEntry.destination.route!!.routeToScreenDestinationClass()
                        currentDestination = getDestination(navBackStackEntry, destinationClass)
                    }
                    purgeScreenDestinationsMap()
                    _currentEntryFlow.value = currentDestination
                }
                .launchIn(coroutineScope)
        }

        override val size: Int
            get() = navHostController.currentBackStack.value.size - 1

        override val currentDestinationFlow: StateFlow<ScreenDestination> = _currentEntryFlow.asStateFlow()

        override fun lastIndexOf(destination: ScreenDestination): Int {
            if (destination::class.objectInstance != null) return lastIndexOf(destination::class)
            val baseRoute = destination.baseRoute
            val argument = destination.argument?.let { "{$it}" }
            return navHostController.currentBackStack.value
                .indexOfLast { it.destination.route == baseRoute && it.arguments?.getString(ArgumentsName) == argument }
                .let { if (it >= 0) it - 1 else it }
        }

        override fun lastIndexOf(destination: GraphDestination): Int {
            val baseRoute = destination.baseRoute
            return navHostController.currentBackStack.value
                .indexOfLast { it.destination.route == baseRoute }
                .let { if (it >= 0) it - 1 else it }
        }

        private fun purgeScreenDestinationsMap() {
            val backStack = navHostController.currentBackStack.value
            backStack.forEach { navBackStackEntry ->
                navBackStackEntry.id.let {
                    if (screenDestinationsMap.contains(it).not()) {
                        screenDestinationsMap.remove(it)
                    }
                }
            }
        }
    }

    override val backStack: Navigator.BackStack = JetpackBackStack()

    override fun setResultForCurrentDestination(result: Any) {
        val currentBackStackEntry = navHostController.currentBackStackEntry ?: return
        currentBackStackEntry.emitNavigationResult(result)
    }

    override fun doNavigateBack() {
        navHostController.popBackStack()
    }

    override fun doPopUpTo(popUpTo: NavigatorDestination.PopUpTo) {
        when (popUpTo) {
            is NavigatorDestination.PopUpTo.FirstDestination ->
                doPopUpToFirst(popUpTo.inclusive, popUpTo.replaceWith)

            is NavigatorDestination.PopUpTo.CurrentDestination -> {
                val destinationClass = backStack.currentDestinationFlow.value::class
                doPopUpTo(destinationClass, popUpTo.inclusive, popUpTo.replaceWith)
            }

            is NavigatorDestination.PopUpTo.DestinationInstance ->
                doPopUpTo(popUpTo.destination, popUpTo.inclusive, popUpTo.replaceWith)

            is NavigatorDestination.PopUpTo.DestinationClass ->
                doPopUpTo(popUpTo.destination, popUpTo.inclusive, popUpTo.replaceWith)
        }
    }

    private fun doPopUpToFirst(inclusive: Boolean, replaceWith: ScreenDestination?) = with(navHostController) {
        if (inclusive) {
            if (replaceWith != null) {
                repeat(currentBackStack.value.size - 2) { popBackStack() }
                navigateToScreenDestination(replaceWith) {
                    popUpTo(route = currentBackStack.value[1].destination.route!!) {
                        this.inclusive = true
                    }
                }
            } else error("Backstack can not be empty")
        } else {
            repeat(currentBackStack.value.size - 2) { popBackStack() }
            replaceWith?.let { navigateTo(it) }
        }
    }

    private fun doPopUpTo(destination: ScreenDestination, inclusive: Boolean, replaceWith: ScreenDestination?) {
        // NavHostController doesn't support popup to the same destination route if there are multiple instances of it
        // This is workaround for this case
        val screenIndex = backStack.lastIndexOf(destination)
        if (screenIndex < 0) return
        val popupCount = backStack.size - screenIndex + if (inclusive) 0 else -1
        if (popupCount > 0) {
            repeat(popupCount) { navHostController.popBackStack() }
        }
        if (replaceWith != null) navHostController.navigateToScreenDestination(replaceWith)
    }

    private fun doPopUpTo(destination: GraphDestination, inclusive: Boolean, replaceWith: ScreenDestination?) {
        if (replaceWith != null) {
            navHostController.navigateToScreenDestination(replaceWith) {
                popUpTo(route = destination.baseRoute) {
                    this.inclusive = inclusive
                }
            }
        } else {
            navHostController.popBackStack(
                route = destination.baseRoute,
                inclusive = inclusive,
            )
        }
    }

    override fun doPush(destination: ScreenDestination) {
        navHostController.navigateToScreenDestination(destination)
    }
}

private inline fun NavController.navigateToScreenDestination(
    destination: ScreenDestination,
    noinline builder: (NavOptionsBuilder.() -> Unit)? = null,
) {
    navigate(destination.route, builder?.let { navOptions(it) })
}
