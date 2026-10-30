package amaterek.util.ui.navigation.sample.ui.screen.splash

import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data object RootSplashDestination : ScreenDestination {

    @Composable
    override fun Content() {
        RootSplashScreen()
    }
}
