package amaterek.util.ui.navigation.sample.ui.screen.main

import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.serialization.Serialize
import androidx.compose.runtime.Composable

@Serialize
data object RootMainDestination : ScreenDestination {

    @Composable
    override fun Content() {
        RootMainScreen()
    }
}