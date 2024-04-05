package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Stable
import androidx.compose.ui.window.DialogProperties

@Stable
interface DialogDestination : ScreenDestination, DialogPropertiesProvider

interface DialogPropertiesProvider {

    val dialogProperties: DialogProperties
        get() = DialogProperties()
}