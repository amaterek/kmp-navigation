package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Stable

@Stable
fun PreviousDestination.withResult(result: Any): NavigatorDestination =
    NavigatorDestination.WithResult(destination = this, result = result)

@Stable
fun Destination.withResult(result: Any): NavigatorDestination =
    NavigatorDestination.WithResult(destination = this, result = result)

@Stable
fun ScreenDestination.replace(): NavigatorDestination =
    NavigatorDestination.PopUpTo.CurrentDestination(inclusive = true, replaceWith = this)

@Stable
fun ScreenDestination.replaceAll(): NavigatorDestination =
    NavigatorDestination.PopUpTo.FirstDestination(inclusive = true, replaceWith = this)

@Stable
fun ScreenDestination.popUpToFirst(): NavigatorDestination =
    NavigatorDestination.PopUpTo.FirstDestination(inclusive = false, replaceWith = this)

@Stable
fun ScreenDestination.popUpTo(
    destination: ScreenDestination,
    inclusive: Boolean = false,
): NavigatorDestination =
    NavigatorDestination.PopUpTo.DestinationInstance(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
    )

@Stable
fun ScreenDestination.popUpTo(
    destination: GraphDestination,
    inclusive: Boolean = false,
): NavigatorDestination =
    NavigatorDestination.PopUpTo.DestinationClass(
        destination = destination,
        inclusive = inclusive,
        replaceWith = this,
    )

@Stable
fun Destination.redirectToParentIfNotInGraph(): Destination =
    NavigatorDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.IfNotInCurrentGraph,
    )

@Stable
fun Destination.redirectToParent(deep: Int = 1): Destination =
    NavigatorDestination.RedirectToParent(
        destination = this,
        strategy = RedirectToParentStrategy.Deep(deep),
    )
