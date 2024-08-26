package amaterek.util.ui.navigation.sample.ui.screen.withargumentandforresult

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.serialization.Serializable
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransitionProvider
import amaterek.util.ui.navigation.transition.SlideVerticallyScreenTransition
import androidx.compose.runtime.Composable

data class RootWithArgumentAndForResultDestination(
    private val text: String,
) : ScreenDestination, ScreenTransitionProvider {

    override val transition: ScreenTransition
        get() = Companion.transition

    @Composable
    override fun Content() {
        RootWithArgumentAndForResultScreen(text)
    }

    data class WithArgumentAndForResultDestinationResult(val value: String) : Serializable

    companion object : ScreenTransitionProvider {
        // For JetPack navigator (for non Object destinations)
        override val transition: ScreenTransition = SlideVerticallyScreenTransition
    }
}