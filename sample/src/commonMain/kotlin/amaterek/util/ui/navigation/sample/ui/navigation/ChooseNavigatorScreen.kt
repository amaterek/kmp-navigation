package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.ui.navigation.sample.ComposeApp
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
import androidx.compose.ui.unit.dp

@Composable
fun ChooseNavigatorScreen(platformNavigation: PlatformNavigation) {
    val chosenNavigator = rememberSaveable {
        mutableStateOf<ChosenNavigator?>(null)
    }
    when (chosenNavigator.value) {
        null -> ChooseNavigatorView { chosenNavigator.value = it }
        ChosenNavigator.Voyager -> ComposeApp(platformNavigation, GetVoyagerNavigationHost())
        ChosenNavigator.Jetpack -> ComposeApp(platformNavigation, GetJetpackNavigationHost())
        ChosenNavigator.Nav3 -> ComposeApp(platformNavigation, GetNav3NavigationHost())
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
            Text("Jetpack Navigator")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onNavigatorChosen(ChosenNavigator.Nav3) },
        ) {
            Text("Navigation 3 Navigator")
        }
    }
}

private enum class ChosenNavigator {
    Voyager,
    Jetpack,
    Nav3,
}
