package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.serialization.Serializable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * The interface must be implemented by all destination that are used by default navigator.
 *
 * If destination is without parameters `data object` should be used:
 * - The class may implement [amaterek.util.ui.navigation.transition.ScreenTransitionProvider]
 *   if non default transition should be used for the screen.
 *
 * If destination has parameters `data class` should be used:
 * - All parameters must be [Serializable]`.
 *   WARNING: All fields will be serialized. To exclude from serialization use [amaterek.util.ui.navigation.serialization.SkipForSerialization] annotation.
 * - When used with [JetpackNavigator] the `companion object` may implement [amaterek.util.ui.navigation.transition.ScreenTransitionProvider]
 *   if non default transition should be used for the screen.
 *   When used with [VoyagerNavigator] class may implement [amaterek.util.ui.navigation.transition.ScreenTransitionProvider]
 *  *   if non default transition should be used for the screen.
 */
@Stable
interface ScreenDestination : NavigatorDestination, Serializable {

    @Composable
    fun Content()
}
