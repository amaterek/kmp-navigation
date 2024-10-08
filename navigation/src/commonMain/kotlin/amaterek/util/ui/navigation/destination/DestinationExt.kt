package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.annotation.InternalNavigation
import androidx.compose.runtime.Stable

/**
 * Adds result to destination.
 *
 * To observe result use [Navigator.resultFlow] in destination that expects the result.
 */
@Stable
fun PreviousDestination.withResult(result: Any) =
    PreviousDestinationWithResult(result = result)

/**
 * Adds result to destination.
 *
 * To observe result use [Navigator.resultFlow] in destination that expects the result.
 */
@OptIn(InternalNavigation::class)
@Stable
fun NavigatorDestination.PopUpTo.withResult(result: Any): NavigatorDestination.PopUpTo = when (this) {
    is NavigatorDestination.PopUpTo.FirstDestination -> copy(result = result)
    is NavigatorDestination.PopUpTo.CurrentDestination -> copy(result = result)
    is NavigatorDestination.PopUpTo.DestinationInstance -> copy(result = result)
    is NavigatorDestination.PopUpTo.DestinationClass -> copy(result = result)
}

/**
 * Replaces current destination with the destination.
 */
@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.replace(): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.CurrentDestination(inclusive = true, replaceWith = this, result = null)

/**
 * Replaces whole back stack with the destination.
 */
@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.replaceAll(): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.FirstDestination(inclusive = true, replaceWith = this, result = null)

/**
 * Replaces all back stack with the destination.
 */
@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.popUpToFirst(): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.FirstDestination(inclusive = false, replaceWith = this, result = null)

/**
 * Popups to specific the destination instance and adds the destination to the backstack.
 */
@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.popUpTo(
    destination: ScreenDestination,
    inclusive: Boolean = false,
): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.DestinationInstance(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
        result = null,
    )

/**
 * Popups to the destination class and adds the destination to the backstack.
 */
@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.popUpTo(
    destination: GraphDestination,
    inclusive: Boolean = false,
): NavigatorDestination.PopUpTo =
    NavigatorDestination.PopUpTo.DestinationClass(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
        result = null,
    )

/**
 * Redirect the destination to parent navigator if the destination is not in current graph.
 */
@OptIn(InternalNavigation::class)
@Stable
fun Destination.redirectToParentIfNotInGraph(): Destination =
    NavigatorDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.IfNotInCurrentGraph,
    )

/**
 * Redirects the destination to parent navigator [deep] times.
 */
@OptIn(InternalNavigation::class)
@Stable
fun Destination.redirectToParent(deep: Int = 1): Destination =
    NavigatorDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.Deep(deep),
    )
