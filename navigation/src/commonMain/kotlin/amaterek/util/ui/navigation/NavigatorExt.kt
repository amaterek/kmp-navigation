@file:Suppress("NOTHING_TO_INLINE", "TooManyFunctions")

package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.PopUpToDestination
import amaterek.util.ui.navigation.destination.PopUpToFirstDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.popUpTo
import amaterek.util.ui.navigation.destination.popUpToFirst
import amaterek.util.ui.navigation.destination.replace
import amaterek.util.ui.navigation.destination.replaceAll
import amaterek.util.ui.navigation.destination.withResult
import amaterek.util.ui.navigation.internal.NavigationResultFlow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlinx.coroutines.flow.StateFlow

inline fun Navigator.canNavigateBack() =
    backStack.size > 1

inline fun Navigator.navigateBack() =
    navigateTo(PreviousDestination)

inline fun Navigator.navigateBackWithResult(result: Any) =
    navigateTo(PreviousDestination.withResult(result))

inline fun Navigator.replace(destination: ScreenDestination) =
    navigateTo(destination.replace())

inline fun Navigator.replaceAll(destination: ScreenDestination) =
    navigateTo(destination.replaceAll())

inline fun Navigator.popUpToFirst(replaceWith: ScreenDestination? = null) =
    replaceWith?.popUpToFirst() ?: navigateTo(PopUpToFirstDestination())

inline fun Navigator.popUpToFirstWithResult(result: Any) =
    navigateTo(PopUpToFirstDestination().withResult(result))

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

inline fun Navigator.popUpTo(destination: ScreenDestination, replaceWith: ScreenDestination, inclusive: Boolean = false) =
    navigateTo(
        replaceWith.popUpTo(
            destination = destination,
            inclusive = inclusive,
        ),
    )

inline fun Navigator.popUpTo(destination: GraphDestination, replaceWith: ScreenDestination, inclusive: Boolean = false) =
    navigateTo(
        replaceWith.popUpTo(
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

@Suppress("UnusedReceiverParameter")
@Deprecated("Will be removed", replaceWith = ReplaceWith("LocalNavigationResultFlow"))
inline val Navigator.resultFlow: NavigationResultFlow
    @ReadOnlyComposable
    @Composable
    get() = LocalNavigationResultFlow.current
