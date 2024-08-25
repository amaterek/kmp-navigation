package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.annotation.InternalNavigation
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
sealed interface NavigatorDestination : Destination {

    data class WithResult(val destination: Destination, val result: Any) : NavigatorDestination

    @Immutable
    sealed interface PopUpTo : NavigatorDestination {

        val inclusive: Boolean
        val replaceWith: ScreenDestination?

        @InternalNavigation
        data class FirstDestination(
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo

        @InternalNavigation
        data class CurrentDestination(
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo

        @InternalNavigation
        data class DestinationInstance(
            val destination: ScreenDestination,
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo

        @InternalNavigation
        data class DestinationClass(
            val destination: GraphDestination,
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
        ) : PopUpTo
    }

    @InternalNavigation
    data class RedirectToParent(
        val destination: Destination,
        val strategy: RedirectToParentStrategy,
    ) : NavigatorDestination
}

@Immutable
sealed interface RedirectToParentStrategy {

    data object IfNotInCurrentGraph : RedirectToParentStrategy

    data class Deep(val value: Int) : RedirectToParentStrategy
}

@OptIn(InternalNavigation::class)
@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToDestination(destination: ScreenDestination, inclusive: Boolean = false): Destination =
    NavigatorDestination.PopUpTo.DestinationInstance(
        destination = destination,
        inclusive = inclusive,
        replaceWith = null,
    )

@OptIn(InternalNavigation::class)
@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToDestination(destination: GraphDestination, inclusive: Boolean = false): NavigatorDestination =
    NavigatorDestination.PopUpTo.DestinationClass(
        destination = destination,
        inclusive = inclusive,
        replaceWith = null,
    )

@OptIn(InternalNavigation::class)
@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToFirstDestination(): Destination =
    NavigatorDestination.PopUpTo.FirstDestination(
        inclusive = false,
        replaceWith = null,
    )
