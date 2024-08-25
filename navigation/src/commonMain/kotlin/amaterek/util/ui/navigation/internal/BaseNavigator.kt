package amaterek.util.ui.navigation.internal

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.canNavigateBack
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination.PopUpTo
import amaterek.util.ui.navigation.destination.NavigatorDestination.RedirectToParent
import amaterek.util.ui.navigation.destination.NavigatorDestination.WithResult
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.RedirectToParentStrategy
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.withResult

@Suppress("TooManyFunctions")
@InternalNavigation
abstract class BaseNavigator(
    private val graph: Set<GraphDestination>,
    private val parent: Navigator?,
) : Navigator {

    override fun navigateTo(destination: Destination) {
        doNavigateTo(destination, result = null)
    }

    private fun doNavigateTo(destination: Destination, result: Any?) {
        when (destination) {
            PreviousDestination -> navigateToPreviousDestination(result)
            is NavigatorDestination -> handleControlDestination(destination, result)
            else -> destinationIsNotSupportedError(destination)
        }
    }

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private fun handleControlDestination(destination: NavigatorDestination, result: Any?) = when (destination) {
        is ScreenDestination ->
            if (graph.contains(destination::class)) {
                doPush(destination)
            } else destinationNotFoundInGraphError(destination)

        is WithResult -> {
            when (val target = destination.destination) {
                PreviousDestination -> doNavigateTo(target, destination.result)
                is PopUpTo -> doNavigateTo(target, destination.result)
                else -> destinationIsNotSupportedForWithResultError(destination.destination)
            }
        }

        is PopUpTo -> {
            destination.requireInBackstack()
            doPopUpTo(destination)
            setResult(result)
        }

        is RedirectToParent -> parent?.let {
            when (destination.strategy) {
                RedirectToParentStrategy.IfNotInCurrentGraph -> {
                    if (!graph.contains(destination.destination::class)) {
                        it.navigateTo(destination)
                    } else {
                        navigateTo(destination.destination)
                    }
                }
                is RedirectToParentStrategy.Deep -> {
                    val times = destination.strategy.value
                    if (times > 1) {
                        it.navigateTo(
                            RedirectToParent(
                                destination = destination,
                                strategy = RedirectToParentStrategy.Deep(times - 1),
                            ),
                        )
                    } else {
                        it.navigateTo(destination.destination)
                    }
                }
            }
        } ?: noParentNavigatorError(destination)
    }

    private fun navigateToPreviousDestination(result: Any?) {
        if (canNavigateBack()) {
            doNavigateBack()
            result?.let { setResult(result) }
        } else redirectToParent(PreviousDestination, result)
    }

    private fun redirectToParent(destination: Destination, result: Any?) {
        parent?.navigateTo(
            result?.let { destination.withResult(result) } ?: destination,
        ) ?: noParentNavigatorError(destination)
    }

    private fun PopUpTo.requireInBackstack() = when (this) {
        is PopUpTo.DestinationInstance ->
            if (backStack.lastIndexOf(destination) < 0) destinationIsNotInBackStack(destination::class) else Unit

        is PopUpTo.DestinationClass ->
            if (backStack.lastIndexOf(destination) < 0) destinationIsNotInBackStack(destination) else Unit

        else -> Unit
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun noParentNavigatorError(destination: Destination): Nothing =
        error("No parent navigator for: $destination")

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun destinationNotFoundInGraphError(destination: Destination): Nothing =
        error("Destination $destination not found in the graph in the navigator: ${destination::class.simpleName}")

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun destinationIsNotSupportedError(destination: Destination): Nothing =
        error("Requested not supported destination: $destination")

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun destinationIsNotSupportedForWithResultError(destination: Destination): Nothing =
        error("Requested not supported destination for result: $destination")

    private fun destinationIsNotInBackStack(destination: GraphDestination): Nothing =
        error("Requested destination is not in backstack: $destination")

    protected abstract fun doNavigateBack()

    protected abstract fun doPopUpTo(popUpTo: PopUpTo)

    protected abstract fun doPush(destination: ScreenDestination)
}