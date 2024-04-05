package amaterek.util.ui.navigation.sample.ui.screen.splash

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.Composable

data object RootSplashDestination : ScreenDestination {

    override val transition: ScreenTransition = FadeScreenTransition

    @Composable
    override fun Content() {
        RootSplashScreen()
    }
}