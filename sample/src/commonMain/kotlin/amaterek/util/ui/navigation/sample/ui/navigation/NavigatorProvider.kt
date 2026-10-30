package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.ui.navigation.NavigationGraph
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.VoyagerNavigationHost
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
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
        graph: NavigationGraph,
        parent: Navigator?,
    ): Navigator
}

internal class GetVoyagerNavigationHost : NavigatorProvider {

    @Composable
    override fun invoke(
        navigator: Navigator,
    ) {
        VoyagerNavigationHost(navigator)
    }

    @Composable
    override fun rememberNavigator(
        startDestination: ScreenDestination,
        graph: NavigationGraph,
        parent: Navigator?,
    ): Navigator = rememberVoyagerNavigator(startDestination, graph, parent)
}

internal class GetJetpackNavigationHost : NavigatorProvider {

    @Composable
    override fun invoke(
        navigator: Navigator,
    ) {
        JetpackNavigationHost(navigator)
    }

    @Composable
    override fun rememberNavigator(
        startDestination: ScreenDestination,
        graph: NavigationGraph,
        parent: Navigator?,
    ): Navigator = rememberJetpackNavigator(startDestination, graph, parent)
}
