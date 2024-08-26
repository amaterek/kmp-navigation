package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.serialization.Serializable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface ScreenDestination : NavigatorDestination, Serializable {

    @Composable
    fun Content()
}
