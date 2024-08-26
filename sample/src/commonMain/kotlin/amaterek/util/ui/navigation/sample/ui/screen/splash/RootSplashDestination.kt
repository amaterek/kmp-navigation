package amaterek.util.ui.navigation.sample.ui.screen.splash

import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Composable

data object RootSplashDestination : ScreenDestination {

    @Composable
    override fun Content() {
        RootSplashScreen()
    }
}