@file:Suppress("NOTHING_TO_INLINE")

package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.ControlDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.BaseNavigator
import android.annotation.SuppressLint
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.yield

@SuppressLint("RestrictedApi")
@OptIn(InternalNavigation::class)
class JetpackNavigator(
    internal val startDestination: ScreenDestination,
    internal val navHostController: NavHostController,
    internal val graph: Set<GraphDestination>,
    private val coroutineScope: CoroutineScope,
    parent: Navigator?,
) : BaseNavigator(graph, parent) {

    private val screenDestinationsMap = mutableMapOf<String, ScreenDestination>()

    internal fun onDestinationCreated(navBackStackEntryId: String, destination: ScreenDestination) {
        screenDestinationsMap[navBackStackEntryId] = destination
    }

    private inner class JetpackBackStack : Navigator.BackStack {

        private val _currentEntryFlow = MutableStateFlow(startDestination)

        init {
            navHostController.currentBackStackEntryFlow
                .onEach {
                    var currentDestination: ScreenDestination? = null
                    yield()
                    @Suppress("MagicNumber")
                    var repeatCounter = 500
                    while (--repeatCounter >= 0 && currentDestination == null) {
                        delay(1)
                        currentDestination = screenDestinationsMap[it.id]
                    }
                    purgeScreenDestinationsMap()
                    _currentEntryFlow.value = currentDestination!!
                }
                .launchIn(coroutineScope)
        }

        override val size: Int
            get() = navHostController.currentBackStack.value.size - 1

        override val currentDestinationFlow: StateFlow<ScreenDestination> = _currentEntryFlow.asStateFlow()

        override fun lastIndexOf(destination: ScreenDestination): Int {
            val baseRoute = destination.baseRoute
            val argument = destination.argument?.let { "{$it}" }
            return navHostController.currentBackStack.value.indexOfLast {
                it.destination.route == baseRoute && it.arguments?.getString(ArgumentsName) == argument
            }
        }

        override fun lastIndexOf(destination: GraphDestination): Int {
            val baseRoute = destination.baseRoute
            return navHostController.currentBackStack.value.indexOfLast { it.destination.route == baseRoute }
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

    override fun getResultFlowForDestination(destination: ScreenDestination): Flow<Any?> {
        // FIXME Get proper backstack entry
        val currentBackStackEntry = requireNotNull(navHostController.currentBackStackEntry)
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        return savedStateHandle.getStateFlow(ResultFlowKey, null)
    }

    override fun setResult(result: Any?) {
        val currentBackStackEntry = navHostController.currentBackStackEntry ?: return
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle[ResultFlowKey] = result
    }

    override fun doNavigateBack() {
        navHostController.popBackStack()
    }

    override fun doPopUpTo(popUpTo: ControlDestination.PopUpTo) {
        when (popUpTo) {
            is ControlDestination.PopUpTo.CurrentDestination ->
                doPopUpTo(backStack.currentDestinationFlow.value::class, popUpTo.inclusive, popUpTo.replaceWith)

            is ControlDestination.PopUpTo.DestinationInstance ->
                doPopUpTo(popUpTo.destination, popUpTo.inclusive, popUpTo.replaceWith)

            is ControlDestination.PopUpTo.DestinationClass ->
                doPopUpTo(popUpTo.destination, popUpTo.inclusive, popUpTo.replaceWith)
        }
    }

    private fun doPopUpTo(destination: ScreenDestination, inclusive: Boolean, replaceWith: ScreenDestination?) {
        // NavHostController doesn't support popup to the same destination route if there are multiple instances of it
        // This is workaround for this case
        val screenIndex = backStack.lastIndexOf(destination)
        if (screenIndex < 0) return
        val popupCount = backStack.size - screenIndex + if (inclusive) 1 else 0
        if (popupCount > 0) {
            repeat(popupCount) { navHostController.popBackStack() }
        }
        if (replaceWith != null) navHostController.navigate(replaceWith)
    }

    private fun doPopUpTo(destination: GraphDestination, inclusive: Boolean, replaceWith: ScreenDestination?) {
        if (replaceWith != null) {
            navHostController.navigate(replaceWith) {
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
        navHostController.navigate(destination)
    }

    override fun doReplaceAll(destination: ScreenDestination) {
        navHostController.run {
            currentBackStack.value.firstOrNull()?.let { popUpToNavDestination ->
                navigate(destination) {
                    popUpTo(id = popUpToNavDestination.destination.id) {
                        inclusive = true
                    }
                }
            } ?: navigate(destination)
        }
    }
}

private inline fun NavController.navigate(destination: ScreenDestination) {
    navigate(destination.route)
}

private inline fun NavController.navigate(
    destination: ScreenDestination,
    noinline builder: NavOptionsBuilder.() -> Unit,
) {
    navigate(destination.route, navOptions(builder))
}

private const val ResultFlowKey = "navigation_result"