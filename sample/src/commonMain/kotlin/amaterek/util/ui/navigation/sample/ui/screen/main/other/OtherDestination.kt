package amaterek.util.ui.navigation.sample.ui.screen.main.other

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransitionProvider
import androidx.compose.runtime.Composable

data object OtherDestination : ScreenDestination, ScreenTransitionProvider {

    override val transition: ScreenTransition = FadeScreenTransition

    @Composable
    override fun Content() {
        OtherScreen()
    }
}