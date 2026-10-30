package amaterek.util.ui.navigation.sample.ui.screen.main.other

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data object OtherDestination : ScreenDestination {

    override val transition: ScreenTransition
        get() = FadeScreenTransition

    @Composable
    override fun Content() {
        OtherScreen()
    }
}
