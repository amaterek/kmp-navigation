package amaterek.util.ui.navigation.sample.ui.screen.main

import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
data object RootMainDestination : ScreenDestination {

    @Composable
    override fun Content() {
        RootMainScreen()
    }
}
