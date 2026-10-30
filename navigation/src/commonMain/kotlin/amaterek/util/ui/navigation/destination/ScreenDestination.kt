package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.serialization.Serializable
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * The interface must be implemented by all destination that are used by default navigator.
 *
 * If destination is without parameters `data object` should be used.
 *
 * If destination has parameters `data class` should be used:
 * - All parameters must be [Serializable]`.
 *   WARNING: All fields will be serialized. To exclude from serialization use [amaterek.util.ui.navigation.serialization.SkipForSerialization] annotation.
 *
 * Override [transition] if non default transition should be used for the screen. In a `data class` override it
 * with a getter (`override val transition get() = ...`), so it isn't serialized.
 */
@Stable
interface ScreenDestination : NavigatorDestination, Serializable {

    /**
     * The transition of the screen or `null` to use the default transition of the navigator.
     * Not used by [DialogDestination]s.
     */
    val transition: ScreenTransition?
        get() = null

    @Composable
    fun Content()
}
