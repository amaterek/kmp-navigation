package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.PopUpToDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.withResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

inline fun Navigator.canNavigateBack() =
    backStack.size > 1

inline fun Navigator.navigateBack() =
    navigateTo(PreviousDestination)

inline fun Navigator.navigateBackWithResult(result: Any) =
    navigateTo(PreviousDestination.withResult(result))

inline fun Navigator.popUpTo(destination: GraphDestination, inclusive: Boolean = false) =
    navigateTo(
        PopUpToDestination(
            destination = destination,
            inclusive = inclusive,
        ),
    )

inline fun Navigator.popUpTo(destination: ScreenDestination, inclusive: Boolean = false) =
    navigateTo(
        PopUpToDestination(
            destination = destination,
            inclusive = inclusive,
        ),
    )

inline fun Navigator.popUpToWithResult(destination: GraphDestination, result: Any, inclusive: Boolean = false) =
    navigateTo(
        PopUpToDestination(
            destination = destination,
            inclusive = inclusive,
        ).withResult(result),
    )

inline fun Navigator.popUpToWithResult(destination: ScreenDestination, result: Any, inclusive: Boolean = false) =
    navigateTo(
        PopUpToDestination(
            destination = destination,
            inclusive = inclusive,
        ).withResult(result),
    )

inline val Navigator.currentDestinationFlow: StateFlow<ScreenDestination>
    get() = backStack.currentDestinationFlow

@OptIn(InternalNavigation::class)
inline val Navigator.resultFlow: Flow<Any?>
    @ReadOnlyComposable
    @Composable
    get() = getResultFlowForDestination(LocalDestination.current)
