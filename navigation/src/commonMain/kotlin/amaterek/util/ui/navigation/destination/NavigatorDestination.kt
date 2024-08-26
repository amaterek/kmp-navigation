package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.annotation.InternalNavigation
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
sealed interface NavigatorDestination : Destination {

    @Immutable
    sealed interface PopUpTo : NavigatorDestination {

        val inclusive: Boolean
        val replaceWith: ScreenDestination?
        val result: Any?

        @InternalNavigation
        data class FirstDestination(
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
            override val result: Any?,
        ) : PopUpTo

        @InternalNavigation
        data class CurrentDestination(
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
            override val result: Any?,
        ) : PopUpTo

        @InternalNavigation
        data class DestinationInstance(
            val destination: ScreenDestination,
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
            override val result: Any?,
        ) : PopUpTo

        @InternalNavigation
        data class DestinationClass(
            val destination: GraphDestination,
            override val inclusive: Boolean,
            override val replaceWith: ScreenDestination?,
            override val result: Any?,
        ) : PopUpTo
    }

    @InternalNavigation
    data class RedirectToParent(
        val destination: Destination,
        val strategy: RedirectToParentStrategy,
    ) : NavigatorDestination
}

@InternalNavigation
@Immutable
sealed interface RedirectToParentStrategy {

    data object IfNotInCurrentGraph : RedirectToParentStrategy

    data class Deep(val value: Int) : RedirectToParentStrategy
}

@OptIn(InternalNavigation::class)
@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToDestination(
    destination: ScreenDestination,
    inclusive: Boolean = false,
    replaceWith: ScreenDestination? = null,
    withResult: Any? = null,
): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.DestinationInstance(
        destination = destination,
        inclusive = inclusive,
        replaceWith = replaceWith,
        result = withResult,
    )

@OptIn(InternalNavigation::class)
@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToDestination(
    destination: GraphDestination,
    inclusive: Boolean = false,
    replaceWith: ScreenDestination? = null,
    withResult: Any? = null,
): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.DestinationClass(
        destination = destination,
        inclusive = inclusive,
        replaceWith = replaceWith,
        result = withResult,
    )

@OptIn(InternalNavigation::class)
@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToFirstDestination(
    replaceWith: ScreenDestination? = null,
    withResult: Any? = null,
): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.FirstDestination(
        inclusive = false,
        replaceWith = replaceWith,
        result = withResult,
    )

@OptIn(InternalNavigation::class)
@Suppress("FunctionName", "NOTHING_TO_INLINE")
@Stable
inline fun PopUpToCurrentDestination(
    replaceWith: ScreenDestination? = null,
): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.FirstDestination(
        inclusive = false,
        replaceWith = replaceWith,
        result = null,
    )
