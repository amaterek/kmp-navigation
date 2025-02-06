package amaterek.util.ui.navigation.sample.ui

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.VoyagerNavigationHost
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.rememberVoyagerNavigator
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChooseNavigatorScreen(
    startDestination: ScreenDestination,
    graph: Set<GraphDestination>,
) {
    var chosenNavigator by rememberSaveable {
        mutableStateOf<ChosenNavigator?>(null)
    }

    when (chosenNavigator) {
        null -> ChooseNavigatorView { chosenNavigator = it }

        ChosenNavigator.Voyager -> {
            CompositionLocalProvider(LocalChosenNavigator provides ChosenNavigator.Voyager) {
                VoyagerNavigationHost(
                    rememberVoyagerNavigator(
                        startDestination = startDestination,
                        graph = graph,
                        parent = LocalNavigator.current,
                    ),
                )
            }
        }

        ChosenNavigator.Jetpack ->
            CompositionLocalProvider(LocalChosenNavigator provides ChosenNavigator.Voyager) {
                JetpackNavigationHost(
                    startDestination = startDestination,
                    graph = graph,
                    parent = LocalNavigator.current,
                )
            }
    }
}

@Composable
private fun ChooseNavigatorView(
    onNavigatorChosen: (ChosenNavigator) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigatorChosen(ChosenNavigator.Voyager) },
        ) {
            Text("Voyager Navigator")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigatorChosen(ChosenNavigator.Jetpack) },
        ) {
            Text("JetPack Navigator")
        }
    }
}

@Composable
fun NavigationHost(
    startDestination: ScreenDestination,
    graph: Set<GraphDestination>,
    parent: Navigator? = null,
    defaultTransition: ScreenTransition = FadeScreenTransition,
) = when (LocalChosenNavigator.current) {
    ChosenNavigator.Voyager -> VoyagerNavigationHost(startDestination, graph, parent, defaultTransition)
    ChosenNavigator.Jetpack -> JetpackNavigationHost(startDestination, graph, parent, defaultTransition)
}

@Composable
fun NavigationHost(
    navigator: Navigator,
) = when (LocalChosenNavigator.current) {
    ChosenNavigator.Voyager -> VoyagerNavigationHost(navigator)
    ChosenNavigator.Jetpack -> JetpackNavigationHost(navigator)
}

@Composable
fun rememberNavigator(
    startDestination: ScreenDestination,
    graph: Set<GraphDestination>,
    parent: Navigator?,
    defaultTransition: ScreenTransition = FadeScreenTransition,
): Navigator = when (LocalChosenNavigator.current) {
    ChosenNavigator.Voyager -> rememberVoyagerNavigator(
        startDestination = startDestination,
        graph = graph,
        parent = parent,
        defaultTransition = defaultTransition,
    )
    ChosenNavigator.Jetpack -> rememberJetpackNavigator(
        startDestination = startDestination,
        graph = graph,
        parent = parent,
        defaultTransition = defaultTransition,
    )
}

val LocalChosenNavigator = staticCompositionLocalOf<ChosenNavigator> {
    error("ChosenNavigator han not been provided")
}

enum class ChosenNavigator {
    Voyager,
    Jetpack,
}
