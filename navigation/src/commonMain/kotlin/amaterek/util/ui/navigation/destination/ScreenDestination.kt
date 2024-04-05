package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.serialization.Serializable
import amaterek.util.ui.navigation.transition.ScreenTransitionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface ScreenDestination : Destination, ScreenTransitionProvider, Serializable {

    @Composable
    fun Content()
}
