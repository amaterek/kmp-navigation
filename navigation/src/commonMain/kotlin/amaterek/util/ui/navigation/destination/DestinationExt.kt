package amaterek.util.ui.navigation.destination

import amaterek.util.ui.navigation.annotation.InternalNavigation
import androidx.compose.runtime.Stable

@OptIn(InternalNavigation::class)
@Stable
fun Destination.withResult(result: Any): Destination = when (this) {
    is PreviousDestination,
    is ControlDestination.PopUpTo,
    is ControlDestination.PopUpToClass,
    -> ControlDestination.WithResult(destination = this, result = result)
    else -> invalidDestinationForResultError(this)
}

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.replace(): Destination =
    ControlDestination.Replace(destination = this)

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.replaceAll(): Destination =
    ControlDestination.ReplaceAll(destination = this)

@OptIn(InternalNavigation::class)
@Stable
fun ScreenDestination.popUpTo(
    destination: ScreenDestination,
    inclusive: Boolean = false,
): Destination =
    ControlDestination.PopUpTo(
        popUpTo = destination,
        inclusive = inclusive,
        replaceWith = this,
    )

@OptIn(InternalNavigation::class)
@Stable
fun Destination.toRedirectToParent(strategy: RedirectToParentStrategy = RedirectToParentStrategy.Times(1)): Destination =
    ControlDestination.RedirectToParent(
        destination = this,
        strategy = strategy,
    )

internal fun invalidDestinationForResultError(destination: Destination): Nothing =
    error("Requested not supported destination for result: $destination")

@PublishedApi
internal fun invalidDestinationForPopUpError(destination: Destination): Nothing =
    error("Requested not supported destination for pop up: $destination")