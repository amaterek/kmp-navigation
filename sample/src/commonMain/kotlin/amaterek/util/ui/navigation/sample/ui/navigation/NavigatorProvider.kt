package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.VoyagerNavigationHost
import amaterek.util.ui.navigation.VoyagerNavigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.rememberVoyagerNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

internal interface NavigatorProvider {

    @Stable
    @Composable
    operator fun invoke(
        navigator: Navigator,
    )

    @Composable
    fun rememberNavigator(
        startDestination: ScreenDestination,
        graph: Set<GraphDestination>,
        parent: Navigator?,
    ): Navigator
}

internal class GetDefaultNavigationHost : NavigatorProvider {

    @OptIn(InternalNavigation::class)
    @Composable
    override fun invoke(
        navigator: Navigator,
    ) {
        VoyagerNavigationHost(
            navigator = navigator as VoyagerNavigator,
        )
    }

    @Composable
    override fun rememberNavigator(
        startDestination: ScreenDestination,
        graph: Set<GraphDestination>,
        parent: Navigator?,
    ): Navigator = rememberVoyagerNavigator(startDestination, graph, parent)
}