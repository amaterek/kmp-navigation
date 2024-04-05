package amaterek.util.ui.navigation.sample.ui.screen.main.home

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.Composable

data object HomeDestination : ScreenDestination {

    override val transition: ScreenTransition = FadeScreenTransition

    @Composable
    override fun Content() {
        HomeScreen()
    }
}