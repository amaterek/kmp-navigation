package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Stable
import kotlin.reflect.KClass

/**
 * The base interface of the destination. Can be used to define custom destinations for custom navigators.
 */
@Stable
interface Destination

/**
 * The type of graph's destination.
 */
typealias GraphDestination = KClass<out ScreenDestination>

/**
 * Predefined previous destination to navigate back.
 */
data object PreviousDestination : Destination

/**
 * Predefined previous destination to navigate back with result.
 */
data class PreviousDestinationWithResult(val result: Any) : Destination
