package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.reflect.KClass

@Stable
interface Destination

typealias GraphDestination = KClass<out ScreenDestination>

@Immutable
data object PreviousDestination : Destination

@Suppress("FunctionName")
@Stable
fun PopUpToDestination(destination: ScreenDestination, inclusive: Boolean = false): Destination =
    ControlDestination.PopUpTo.DestinationInstance(
        destination = destination,
        inclusive = inclusive,
        replaceWith = null,
    )

@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToDestination(destination: GraphDestination, inclusive: Boolean = false): Destination =
    ControlDestination.PopUpTo.DestinationClass(
        destination = destination,
        inclusive = inclusive,
        replaceWith = null,
    )

@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToFirstDestination(): Destination =
    ControlDestination.PopUpTo.FirstDestination(
        inclusive = false,
        replaceWith = null,
    )

@Immutable
sealed interface ControlDestination : Destination {

    data class WithResult(val destination: Destination, val result: Any) : ControlDestination

    @Immutable
    sealed interface PopUpTo : ControlDestination {

        val inclusive: Boolean
        val replaceWith: ScreenDestination?

        data class FirstDestination(
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo

        data class CurrentDestination(
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo

        data class DestinationInstance(
            val destination: ScreenDestination,
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo

        data class DestinationClass(
            val destination: GraphDestination,
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo
    }

    data class RedirectToParent(
        val destination: Destination,
        val strategy: RedirectToParentStrategy,
    ) : ControlDestination
}

@Immutable
sealed interface RedirectToParentStrategy {

    data object IfNotInCurrentGraph : RedirectToParentStrategy

    data class Deep(val value: Int) : RedirectToParentStrategy
}
