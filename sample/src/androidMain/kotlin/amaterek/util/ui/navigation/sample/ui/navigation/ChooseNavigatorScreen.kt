package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.sample.ComposeApp
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ChooseNavigatorScreen() {
    val chosenNavigator = rememberSaveable() {
        mutableStateOf<ChosenNavigator?>(null)
    }
    val context = LocalContext.current
    when (chosenNavigator.value) {
        null -> ChooseNavigatorView { chosenNavigator.value = it }
        ChosenNavigator.Multiplatform -> ComposeApp(PlatformNavigation(context), GetDefaultNavigationHost())
        ChosenNavigator.Android -> ComposeApp(PlatformNavigation(context), GetJetpackNavigationHost())
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
            onClick = { onNavigatorChosen(ChosenNavigator.Multiplatform) },
        ) {
            Text("Default (Voyager) Navigator")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigatorChosen(ChosenNavigator.Android) },
        ) {
            Text("JetPack (Android) Navigator")
        }
    }
}

internal class GetJetpackNavigationHost : NavigatorProvider {

    @SuppressLint("ComposableNaming")
    @Composable
    override fun invoke(
        navigator: Navigator,
    ) {
        JetpackNavigationHost(navigator)
    }

    @Composable
    override fun rememberNavigator(
        startDestination: ScreenDestination,
        graph: Set<GraphDestination>,
        parent: Navigator?,
    ): Navigator = rememberJetpackNavigator(startDestination, graph, parent)
}

private enum class ChosenNavigator {
    Multiplatform,
    Android,
}