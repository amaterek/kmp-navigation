package amaterek.util.ui.navigation.internal

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.canNavigateBack
import amaterek.util.ui.navigation.destination.ControlDestination
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.RedirectToParentStrategy
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.invalidDestinationForResultError
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
            PreviousDestination -> {
                if (canNavigateBack()) {
                    doNavigateBack()
                    result?.let { setResult(result) }
                } else redirectToParent(destination, result)
            }

            is ScreenDestination ->
                if (graph.contains(destination::class)) {
                    doPush(destination)
                } else destinationNotFoundInGraphError(destination)

            is ControlDestination -> handleControlDestination(destination, result)

            else -> destinationIsNotSupportedError(destination)
        }
    }

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private fun handleControlDestination(destination: ControlDestination, result: Any?) = when (destination) {
        is ControlDestination.WithResult -> {
            when (val target = destination.destination) {
                PreviousDestination -> doNavigateTo(target, destination.result)

                is ControlDestination.PopUpTo, is ControlDestination.PopUpToClass ->
                    doNavigateTo(target, destination.result)

                else -> destinationIsNotSupportedForWithResultError(destination.destination)
            }
        }

        is ControlDestination.Replace ->
            doPopUpTo(
                popUpTo = backStack.currentDestinationFlow.value,
                inclusive = true,
                replaceWith = destination.destination,
            )

        is ControlDestination.ReplaceAll ->
            doReplaceAll(destination.destination)

        is ControlDestination.PopUpTo -> {
            doPopUpTo(
                popUpTo = destination.popUpTo,
                inclusive = destination.inclusive,
                replaceWith = destination.replaceWith,
            )
            setResult(result)
        }

        is ControlDestination.PopUpToClass -> {
            doPopUpTo(
                popUpTo = destination.popUpTo,
                inclusive = destination.inclusive,
                replaceWith = destination.replaceWith,
            )
            setResult(result)
        }

        is ControlDestination.RedirectToParent -> parent?.let {
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
                            ControlDestination.RedirectToParent(
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

    private fun redirectToParent(destination: Destination, result: Any?) {
        parent?.navigateTo(
            result?.let { destination.withResult(result) } ?: destination,
        ) ?: noParentNavigatorError(destination)
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
        invalidDestinationForResultError(destination)

    protected abstract fun doNavigateBack()

    protected abstract fun doPopUpTo(popUpTo: ScreenDestination, inclusive: Boolean, replaceWith: ScreenDestination?)

    protected abstract fun doPopUpTo(popUpTo: GraphDestination, inclusive: Boolean, replaceWith: ScreenDestination?)

    protected abstract fun doPush(destination: ScreenDestination)

    protected abstract fun doReplaceAll(destination: ScreenDestination)
}