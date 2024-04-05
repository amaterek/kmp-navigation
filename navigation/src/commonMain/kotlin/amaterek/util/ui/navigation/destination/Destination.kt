package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.annotation.InternalNavigation
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.reflect.KClass

@Stable
interface Destination

typealias GraphDestination = KClass<out ScreenDestination>

@Immutable
data object PreviousDestination : Destination

@Suppress("FunctionName")
@OptIn(InternalNavigation::class)
@Stable
inline fun PopUpToDestination(destination: Destination, inclusive: Boolean = false): Destination = when (destination) {
    is ScreenDestination ->
        ControlDestination.PopUpTo(
            popUpTo = destination,
            inclusive = inclusive,
            replaceWith = null,
        )
    else -> invalidDestinationForPopUpError(destination)
}

@Suppress("FunctionName")
@OptIn(InternalNavigation::class)
@Stable
fun PopUpToDestination(destination: GraphDestination, inclusive: Boolean = false): Destination =
    ControlDestination.PopUpToClass(
        popUpTo = destination,
        inclusive = inclusive,
        replaceWith = null,
    )

@InternalNavigation
@Immutable
sealed interface ControlDestination : Destination {

    @Immutable
    data class WithResult(val destination: Destination, val result: Any) : ControlDestination

    @Immutable
    data class Replace(val destination: ScreenDestination) : ControlDestination

    @Immutable
    data class ReplaceAll(val destination: ScreenDestination) : ControlDestination

    @Immutable
    data class PopUpTo(
        val popUpTo: ScreenDestination,
        val inclusive: Boolean,
        val replaceWith: ScreenDestination?,
    ) : ControlDestination

    @Immutable
    data class PopUpToClass(
        val popUpTo: GraphDestination,
        val inclusive: Boolean,
        val replaceWith: ScreenDestination?,
    ) : ControlDestination

    @Immutable
    data class RedirectToParent(
        val destination: Destination,
        val strategy: RedirectToParentStrategy,
    ) : ControlDestination
}

sealed interface RedirectToParentStrategy {

    data object IfNotInCurrentGraph : RedirectToParentStrategy

    data class Times(val value: Int) : RedirectToParentStrategy
}
