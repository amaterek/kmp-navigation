package amaterek.util.ui.navigation.sample.ui.screen.main.home

import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data object HomeDestination : ScreenDestination {

    @Composable
    override fun Content() {
        HomeScreen()
    }
}
