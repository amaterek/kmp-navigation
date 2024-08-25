@file:OptIn(InternalNavigation::class)

package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.ControlDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.BaseNavigator
import amaterek.util.ui.navigation.serialization.SkipForSerialization
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.receiveAsFlow
import cafe.adriel.voyager.navigator.Navigator as VoyagerHavHostController

@InternalNavigation
@Stable
class VoyagerNavigator internal constructor(
    internal val startBackStack: List<ScreenDestination>,
    graph: Set<GraphDestination>,
    parent: Navigator?,
) : BaseNavigator(graph, parent) {

    // HACK Nullable: For Voyager - navigator is available later
    internal var navHostController: VoyagerHavHostController? = null
    internal val currentDestinationFlow = MutableStateFlow(startBackStack.last())

    private inner class VoyagerBackStack : Navigator.BackStack {

        override val size: Int
            get() = navHostController?.size ?: 0

        override val currentDestinationFlow: StateFlow<ScreenDestination>
            get() = this@VoyagerNavigator.currentDestinationFlow.asStateFlow()

        override fun lastIndexOf(destination: ScreenDestination): Int =
            navHostController?.items?.indexOfLast { (it as VoyagerBackStackEntry).destination == destination } ?: -1

        override fun lastIndexOf(destination: GraphDestination): Int =
            navHostController?.items?.indexOfLast { (it as VoyagerBackStackEntry).destination::class == destination } ?: -1
    }

    override val backStack: Navigator.BackStack = VoyagerBackStack()

    override fun getResultFlowForDestination(destination: ScreenDestination): Flow<Any?> {
        val item = navHostController?.items?.lastOrNull { (it as VoyagerBackStackEntry).destination == destination }
        return (item as? VoyagerBackStackEntry)?.resultFlow ?: emptyFlow()
    }

    override fun setResult(result: Any?) {
        val item = navHostController?.lastItem as VoyagerBackStackEntry
        item.getResultChannel().trySend(result)
    }

    override fun doNavigateBack() {
        doIfNavControllerAvailableOrThrow { pop() }
    }

    override fun doPopUpTo(popUpTo: ControlDestination.PopUpTo) {
        when (popUpTo) {
            is ControlDestination.PopUpTo.FirstDestination ->
                doPopUpToFirst(popUpTo.inclusive, popUpTo.replaceWith)

            is ControlDestination.PopUpTo.CurrentDestination ->
                doPopUpToCurrent(popUpTo.inclusive, popUpTo.replaceWith)

            is ControlDestination.PopUpTo.DestinationInstance ->
                doPopUpTo(popUpTo.inclusive, popUpTo.replaceWith) { it == popUpTo.destination }

            is ControlDestination.PopUpTo.DestinationClass ->
                doPopUpTo(popUpTo.inclusive, popUpTo.replaceWith) { it::class == popUpTo.destination }
        }
    }

    private fun doPopUpToFirst(
        inclusive: Boolean,
        replaceWith: ScreenDestination?,
    ) = doIfNavControllerAvailableOrThrow {
        if (inclusive) {
            if (inclusive && replaceWith != null) {
                replaceAll(VoyagerBackStackEntry(replaceWith))
            } else error("Backstack can not be empty")
        } else {
            val firstVoyagerBackStackEntry = items.first()
            popUntil { it === firstVoyagerBackStackEntry }
            replaceWith?.let { push(VoyagerBackStackEntry(it)) }
        }
    }

    private fun doPopUpToCurrent(inclusive: Boolean, replaceWith: ScreenDestination?) = doIfNavControllerAvailableOrThrow {
        when {
            replaceWith == null -> if (inclusive) pop()
            inclusive -> replace(VoyagerBackStackEntry(replaceWith))
            else /* !inclusive */ -> push(VoyagerBackStackEntry(replaceWith))
        }
    }

    private fun doPopUpTo(
        inclusive: Boolean,
        replaceWith: ScreenDestination?,
        condition: (ScreenDestination) -> Boolean,
    ) = doIfNavControllerAvailableOrThrow {
        val replaceWithEntry = replaceWith?.let { VoyagerBackStackEntry(it) }
        val index = items.indexOfLast { condition((it as VoyagerBackStackEntry).destination) }
        when {
            index < 0 -> replaceWithEntry?.let { push(it) }
            index == 0 -> doPopUpToFirst(inclusive, replaceWith)
            index == items.lastIndex -> doPopUpToCurrent(inclusive, replaceWith)
            else -> {
                if (replaceWithEntry != null) {
                    val item = items[index]
                    popUntil { it === item }
                    if (inclusive) replace(replaceWithEntry) else push(replaceWithEntry)
                } else {
                    val item = items[index + if (inclusive) -1 else 0]
                    popUntil { it === item }
                }
            }
        }
    }

    override fun doPush(destination: ScreenDestination) = doIfNavControllerAvailableOrThrow {
        push(VoyagerBackStackEntry(destination))
    }

    private inline fun doIfNavControllerAvailableOrThrow(action: VoyagerHavHostController.() -> Unit) {
        navHostController?.action() ?: error("No Voyager navigation host")
    }
}

@Stable
internal class VoyagerBackStackEntry(
    internal val destination: ScreenDestination,
) : Screen {

    // HACK:
    // By default all fields are serialized
    // To prevent it the annotation is used but when the object is deserialized instance isn't created.
    // We need to create it manually. NOTE 'by lazy` doesn't work also.
    @Suppress("PropertyName")
    @SkipForSerialization
    private var _resultChannel: Channel<Any?>? = null

    internal fun getResultChannel(): Channel<Any?> {
        if (_resultChannel == null) {
            _resultChannel = Channel(onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }
        return requireNotNull(_resultChannel)
    }

    internal val resultFlow: Flow<Any?>
        get() = getResultChannel().receiveAsFlow()

    override val key: ScreenKey = destination.toString() // FIXME check if it is ok?

    @Composable
    override fun Content() {
        destination.Content()
    }
}
