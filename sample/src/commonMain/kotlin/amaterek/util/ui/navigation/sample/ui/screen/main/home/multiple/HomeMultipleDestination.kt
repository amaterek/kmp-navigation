package amaterek.util.ui.navigation.sample.ui.screen.main.home.multiple

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransitionProvider
import amaterek.util.ui.navigation.transition.SlideHorizontallyScreenTransition
import androidx.compose.runtime.Composable

data class HomeMultipleDestination(
    private val level: Int,
) : ScreenDestination, ScreenTransitionProvider {

    override val transition: ScreenTransition
        get() = Companion.transition

    @Composable
    override fun Content() {
        HomeMultipleScreen(level)
    }

    companion object : ScreenTransitionProvider {
        override val transition: ScreenTransition = SlideHorizontallyScreenTransition
    }
}