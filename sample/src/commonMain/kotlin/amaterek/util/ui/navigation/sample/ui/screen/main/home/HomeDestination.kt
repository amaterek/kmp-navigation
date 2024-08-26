package amaterek.util.ui.navigation.sample.ui.screen.main.home

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.serialization.Serialize
import androidx.compose.runtime.Composable

@Serialize
data object HomeDestination : ScreenDestination {

    @Composable
    override fun Content() {
        HomeScreen()
    }
}