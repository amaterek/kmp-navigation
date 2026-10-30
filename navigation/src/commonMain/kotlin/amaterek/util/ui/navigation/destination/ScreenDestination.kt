package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

/**
 * The interface must be implemented by all destination that are used by default navigator.
 *
 * Every destination must be annotated with [kotlinx.serialization.Serializable]:
 * - If destination is without parameters `data object` should be used.
 * - If destination has parameters `data class` should be used.
 *   The destination is stored with its serializer, so all serialized properties must be serializable.
 *   Properties with interface types need their serializers registered in
 *   [amaterek.util.ui.navigation.NavigationGraphBuilder.serializersModule].
 *
 * Override [transition] if non default transition should be used for the screen. In a `data class` override it
 * with a getter (`override val transition get() = ...`), so it isn't serialized.
 *
 * Please check also [DialogDestination].
 */
@Stable
interface ScreenDestination : NavigatorDestination {

    /**
     * The transition of the screen or `null` to use the default transition of the navigator.
     * Not used by [DialogDestination]s.
     */
    val transition: ScreenTransition?
        get() = null

    @Composable
    fun Content()
}
