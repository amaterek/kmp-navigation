package amaterek.util.ui.navigation.sample.ui.screen.splash

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.serialization.Serialize
import androidx.compose.runtime.Composable

@Serialize
data object RootSplashDestination : ScreenDestination {

    @Composable
    override fun Content() {
        RootSplashScreen()
    }
}