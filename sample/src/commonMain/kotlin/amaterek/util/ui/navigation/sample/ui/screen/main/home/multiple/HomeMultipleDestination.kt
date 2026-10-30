package amaterek.util.ui.navigation.sample.ui.screen.main.home.multiple

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.SlideHorizontallyScreenTransition
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data class HomeMultipleDestination(
    private val level: Int,
) : ScreenDestination {

    override val transition: ScreenTransition
        get() = SlideHorizontallyScreenTransition

    @Composable
    override fun Content() {
        HomeMultipleScreen(level)
    }
}
