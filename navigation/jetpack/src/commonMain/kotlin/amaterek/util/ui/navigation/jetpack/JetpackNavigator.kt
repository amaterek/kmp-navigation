package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.NavigationGraph
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.BaseNavigator
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.savedstate.read
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

@Suppress("TooManyFunctions", "RestrictedApi")
@InternalNavigation
class JetpackNavigator(
    private val startDestination: ScreenDestination,
    internal val navHostController: NavHostController,
    internal val graph: NavigationGraph,
    private val coroutineScope: CoroutineScope,
    parent: Navigator?,
    internal val defaultTransition: ScreenTransition,
) : BaseNavigator(graph.destinations, parent) {

    private val json = Json { serializersModule = graph.serializersModule }

    private val entriesByRoute = graph.entries.values.associateBy { it.route }

    internal val startEntry = startDestination.graphEntry()

    internal val startDestinationArgument = encode(startDestination)

    private val screenDestinationsMap = mutableMapOf<String, ScreenDestination>()

    // Results aren't serializable, so they're kept in memory instead of the entry's SavedStateHandle
    private val resultFlowsMap = mutableMapOf<String, MutableStateFlow<Any?>>()

    internal fun getDestination(navBackStackEntry: NavBackStackEntry): ScreenDestination =
        screenDestinationsMap.getOrPut(navBackStackEntry.id) { decode(navBackStackEntry) }

    internal fun transitionOf(navBackStackEntry: NavBackStackEntry): ScreenTransition =
        getDestination(navBackStackEntry).transition ?: defaultTransition

    private inner class JetpackBackStack : Navigator.BackStack {

        private val _currentDestinationFlow = MutableStateFlow(startDestination)

        init {
            // For the back stack changes of the NavHost, e.g. system back
            navHostController.currentBackStackEntryFlow
                .onEach { updateCurrentDestination() }
                .launchIn(coroutineScope)
        }

        fun updateCurrentDestination() {
            val navBackStackEntry = navHostController.currentBackStackEntry ?: return
            val currentDestination = getDestination(navBackStackEntry)
            purgeRemovedEntries()
            _currentDestinationFlow.value = currentDestination
        }

        override val size: Int
            get() = navHostController.currentBackStack.value.size - 1

        override val currentDestinationFlow: StateFlow<ScreenDestination> = _currentDestinationFlow.asStateFlow()

        override fun lastIndexOf(destination: ScreenDestination): Int {
            val route = graph.entries[destination::class]?.route ?: return -1
            return navHostController.currentBackStack.value
                .indexOfLast { it.destination.route == route && getDestination(it) == destination }
                .let { if (it >= 0) it - 1 else it }
        }

        override fun lastIndexOf(destination: GraphDestination): Int {
            val route = graph.entries[destination]?.route ?: return -1
            return navHostController.currentBackStack.value
                .indexOfLast { it.destination.route == route }
                .let { if (it >= 0) it - 1 else it }
        }

        private fun purgeRemovedEntries() {
            val ids = navHostController.currentBackStack.value.mapTo(mutableSetOf()) { it.id }
            screenDestinationsMap.keys.retainAll(ids)
            resultFlowsMap.keys.retainAll(ids)
        }
    }

    private val jetpackBackStack = JetpackBackStack()

    override val backStack: Navigator.BackStack = jetpackBackStack

    // Updated at once, so the current destination of the next navigation in the same frame is right
    override fun navigateTo(destination: Destination) {
        super.navigateTo(destination)
        jetpackBackStack.updateCurrentDestination()
    }

    override fun getResultFlow(navigationResultHandler: Any): Flow<Any?> {
        // JetpackNavigationHost provides the NavBackStackEntry as the navigation result handler
        val backStackEntry = navigationResultHandler as NavBackStackEntry
        return resultFlowOf(backStackEntry)
    }

    override fun setResult(result: Any?) {
        val currentBackStackEntry = navHostController.currentBackStackEntry ?: return
        resultFlowOf(currentBackStackEntry).value = result
    }

    override fun clearResult(navigationResultHandler: Any) {
        resultFlowOf(navigationResultHandler as NavBackStackEntry).value = null
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
            } else {
                error("Backstack can not be empty")
            }
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
        val route = graph.entries.getValue(destination).route
        if (replaceWith != null) {
            navHostController.navigateToScreenDestination(replaceWith) {
                popUpTo(route = route) {
                    this.inclusive = inclusive
                }
            }
        } else {
            navHostController.popBackStack(
                route = route,
                inclusive = inclusive,
            )
        }
    }

    override fun doPush(destination: ScreenDestination) {
        navHostController.navigateToScreenDestination(destination)
    }

    private fun NavController.navigateToScreenDestination(
        destination: ScreenDestination,
        builder: (NavOptionsBuilder.() -> Unit)? = null,
    ) {
        val route = "${destination.graphEntry().name}?$ArgumentsName=${encode(destination).encodeRouteArgument()}"
        if (builder != null) navigate(route, builder) else navigate(route)
    }

    private fun resultFlowOf(navBackStackEntry: NavBackStackEntry): MutableStateFlow<Any?> =
        resultFlowsMap.getOrPut(navBackStackEntry.id) { MutableStateFlow(null) }

    private fun ScreenDestination.graphEntry(): NavigationGraph.Entry =
        graph.entries[this::class] ?: destinationNotFoundInGraphError(this)

    @Suppress("UNCHECKED_CAST")
    private fun encode(destination: ScreenDestination): String =
        json.encodeToString(destination.graphEntry().serializer as KSerializer<ScreenDestination>, destination)

    private fun decode(navBackStackEntry: NavBackStackEntry): ScreenDestination {
        val route = navBackStackEntry.destination.route
        val entry = entriesByRoute[route] ?: error("Unknown destination route: $route")
        val argument = navBackStackEntry.arguments?.read { getStringOrNull(ArgumentsName) }
            ?: error("Missing destination argument for route: $route")
        return json.decodeFromString(entry.serializer, argument)
    }
}
