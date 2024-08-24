package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.annotation.InternalNavigation
import androidx.compose.runtime.Stable

@OptIn(InternalNavigation::class)
@Stable
fun Destination.withResult(result: Any): Destination = when (this) {
    is PreviousDestination,
    is ControlDestination.PopUpTo,
    -> ControlDestination.WithResult(destination = this, result = result)
    else -> invalidDestinationForResultError(this)
}

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.replace(): Destination =
    ControlDestination.PopUpTo.CurrentDestination(inclusive = true, replaceWith = this)

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.replaceAll(): Destination =
    ControlDestination.PopUpTo.FirstDestination(inclusive = true, replaceWith = this)

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.popUpToFirst(): Destination =
    ControlDestination.PopUpTo.FirstDestination(inclusive = false, replaceWith = this)

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.popUpTo(
    destination: ScreenDestination,
    inclusive: Boolean = false,
): Destination =
    ControlDestination.PopUpTo.DestinationInstance(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
    )

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.popUpTo(
    destination: GraphDestination,
    inclusive: Boolean = false,
): Destination =
    ControlDestination.PopUpTo.DestinationClass(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
    )

@OptIn(InternalNavigation::class)
@Stable
fun Destination.redirectToParentIfNotInGraph(): Destination =
    ControlDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.IfNotInCurrentGraph,
    )

@OptIn(InternalNavigation::class)
@Stable
fun Destination.redirectToParent(deep: Int = 1): Destination =
    ControlDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.Deep(deep),
    )

internal fun invalidDestinationForResultError(destination: Destination): Nothing =
    error("Requested not supported destination for result: $destination")

@PublishedApi
internal fun invalidDestinationForPopUpError(destination: Destination): Nothing =
    error("Requested not supported destination for pop up: $destination")