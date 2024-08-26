package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Stable
import androidx.compose.ui.window.DialogProperties

/**
 * The interface must be implemented by all dialog destination that are used by default navigator.
 *
 * If destination is without parameters `data object` should be used:
 * - The class may implement [DialogPropertiesProvider]
 *   if non default dialog properties should be used for the dialog.
 *
 * If destination has parameters `data class` should be used:
 * - When used with [JetpackNavigator] the `companion object` may implement [DialogPropertiesProvider]
 *   if non default dialog properties should be used for the dialog.
 * - When used with [VoyagerNavigator] the class may implement [DialogPropertiesProvider]
 *   if non default dialog properties should be used for the dialog.
 * Please check also [ScreenDestination]
 */
@Stable
interface DialogDestination : ScreenDestination

interface DialogPropertiesProvider {

    val dialogProperties: DialogProperties
        get() = DialogProperties()
}