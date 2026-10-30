package amaterek.util.ui.navigation.sample.ui.screen.withargumentandforresult

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.SlideVerticallyScreenTransition
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data class RootWithArgumentAndForResultDestination(
    private val text: String,
) : ScreenDestination {

    override val transition: ScreenTransition
        get() = SlideVerticallyScreenTransition

    @Composable
    override fun Content() {
        RootWithArgumentAndForResultScreen(text)
    }

    data class WithArgumentAndForResultDestinationResult(val value: String)
}
