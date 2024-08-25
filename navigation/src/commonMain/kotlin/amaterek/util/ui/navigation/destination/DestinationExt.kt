package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Stable

@Stable
fun PreviousDestination.withResult(result: Any): ControlDestination =
    ControlDestination.WithResult(destination = this, result = result)

@Stable
fun Destination.withResult(result: Any): ControlDestination =
    ControlDestination.WithResult(destination = this, result = result)

@Stable
fun ScreenDestination.replace(): ControlDestination =
    ControlDestination.PopUpTo.CurrentDestination(inclusive = true, replaceWith = this)

@Stable
fun ScreenDestination.replaceAll(): ControlDestination =
    ControlDestination.PopUpTo.FirstDestination(inclusive = true, replaceWith = this)

@Stable
fun ScreenDestination.popUpToFirst(): ControlDestination =
    ControlDestination.PopUpTo.FirstDestination(inclusive = false, replaceWith = this)

@Stable
fun ScreenDestination.popUpTo(
    destination: ScreenDestination,
    inclusive: Boolean = false,
): ControlDestination =
    ControlDestination.PopUpTo.DestinationInstance(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
    )

@Stable
fun ScreenDestination.popUpTo(
    destination: GraphDestination,
    inclusive: Boolean = false,
): ControlDestination =
    ControlDestination.PopUpTo.DestinationClass(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
    )

@Stable
fun Destination.redirectToParentIfNotInGraph(): Destination =
    ControlDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.IfNotInCurrentGraph,
    )

@Stable
fun Destination.redirectToParent(deep: Int = 1): Destination =
    ControlDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.Deep(deep),
    )
