@file:OptIn(InternalNavigation::class)

package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.BaseNavigator
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.jvm.Transient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import cafe.adriel.voyager.navigator.Navigator as VoyagerHavHostController

@Suppress("TooManyFunctions")
@InternalNavigation
@Stable
class VoyagerNavigator internal constructor(
    internal val startBackStack: List<ScreenDestination>,
    private val graph: NavigationGraph,
    parent: Navigator?,
    internal val defaultTransition: ScreenTransition,
) : BaseNavigator(graph.destinations, parent) {

    private val json = Json { serializersModule = graph.serializersModule }

    private val entriesByName = graph.entries.values.associateBy { it.name }

    // HACK Nullable: For Voyager - navigator is available later
    internal var navHostController: VoyagerHavHostController? = null
    internal val currentDestinationFlow = MutableStateFlow(startBackStack.last())

    internal fun dialogPropertiesOf(destination: ScreenDestination): DialogProperties? =
        graph.entries[destination::class]?.dialogProperties

    @Suppress("UNCHECKED_CAST")
    internal fun backStackEntryOf(destination: ScreenDestination): VoyagerBackStackEntry {
        val graphEntry = graph.entries[destination::class] ?: destinationNotFoundInGraphError(destination)
        val serializer = graphEntry.serializer as KSerializer<ScreenDestination>
        return VoyagerBackStackEntry(graphEntry.name, json.encodeToString(serializer, destination), destination)
    }

    internal fun destinationOf(backStackEntry: VoyagerBackStackEntry): ScreenDestination =
        backStackEntry.destination ?: run {
            val graphEntry = entriesByName[backStackEntry.name] ?: error("Unknown destination: ${backStackEntry.name}")
            json.decodeFromString(graphEntry.serializer, backStackEntry.encodedDestination)
                .also { backStackEntry.destination = it }
        }

    private val Screen.destination: ScreenDestination
        get() = destinationOf(this as VoyagerBackStackEntry)

    private inner class VoyagerBackStack : Navigator.BackStack {

        override val size: Int
            get() = navHostController?.size ?: 0

        override val currentDestinationFlow: StateFlow<ScreenDestination>
            get() = this@VoyagerNavigator.currentDestinationFlow.asStateFlow()

        override fun lastIndexOf(destination: ScreenDestination): Int =
            navHostController?.items?.indexOfLast { it.destination == destination } ?: -1

        override fun lastIndexOf(destination: GraphDestination): Int =
            navHostController?.items?.indexOfLast { it.destination::class == destination } ?: -1
    }

    override val backStack: Navigator.BackStack = VoyagerBackStack()

    override fun getResultFlow(navigationResultHandler: Any): Flow<Any?> {
        // VoyagerNavigationHost provides the VoyagerBackStackEntry as the navigation result handler
        val backStackEntry = navigationResultHandler as VoyagerBackStackEntry
        return backStackEntry.result
    }

    override fun setResult(result: Any?) {
        val currentBackStackEntry = navHostController?.lastItem as? VoyagerBackStackEntry ?: return
        currentBackStackEntry.result.value = result
    }

    override fun doNavigateBack() {
        doIfNavControllerAvailableOrThrow { pop() }
    }

    override fun doPopUpTo(popUpTo: NavigatorDestination.PopUpTo) {
        when (popUpTo) {
            is NavigatorDestination.PopUpTo.FirstDestination ->
                doPopUpToFirst(popUpTo.inclusive, popUpTo.replaceWith)

            is NavigatorDestination.PopUpTo.CurrentDestination ->
                doPopUpToCurrent(popUpTo.inclusive, popUpTo.replaceWith)

            is NavigatorDestination.PopUpTo.DestinationInstance ->
                doPopUpTo(popUpTo.inclusive, popUpTo.replaceWith) { it == popUpTo.destination }

            is NavigatorDestination.PopUpTo.DestinationClass ->
                doPopUpTo(popUpTo.inclusive, popUpTo.replaceWith) { it::class == popUpTo.destination }
        }
    }

    private fun doPopUpToFirst(
        inclusive: Boolean,
        replaceWith: ScreenDestination?,
    ) = doIfNavControllerAvailableOrThrow {
        if (inclusive) {
            if (replaceWith != null) {
                replaceAll(backStackEntryOf(replaceWith))
            } else error("Backstack can not be empty")
        } else {
            val firstVoyagerBackStackEntry = items.first()
            popUntil { it === firstVoyagerBackStackEntry }
            replaceWith?.let { pushEntry(backStackEntryOf(it)) }
        }
    }

    private fun doPopUpToCurrent(inclusive: Boolean, replaceWith: ScreenDestination?) = doIfNavControllerAvailableOrThrow {
        when {
            replaceWith == null -> if (inclusive) pop()
            inclusive -> replaceEntry(backStackEntryOf(replaceWith))
            else /* !inclusive */ -> pushEntry(backStackEntryOf(replaceWith))
        }
    }

    private fun doPopUpTo(
        inclusive: Boolean,
        replaceWith: ScreenDestination?,
        condition: (ScreenDestination) -> Boolean,
    ) = doIfNavControllerAvailableOrThrow {
        val replaceWithEntry = replaceWith?.let { backStackEntryOf(it) }
        val index = items.indexOfLast { condition(it.destination) }
        when {
            index < 0 -> replaceWithEntry?.let { pushEntry(it) }
            index == 0 -> doPopUpToFirst(inclusive, replaceWith)
            index == items.lastIndex -> doPopUpToCurrent(inclusive, replaceWith)
            else -> {
                if (replaceWithEntry != null) {
                    val item = items[index]
                    popUntil { it === item }
                    if (inclusive) replaceEntry(replaceWithEntry) else pushEntry(replaceWithEntry)
                } else {
                    val item = items[index + if (inclusive) -1 else 0]
                    popUntil { it === item }
                }
            }
        }
    }

    override fun doPush(destination: ScreenDestination) = doIfNavControllerAvailableOrThrow {
        pushEntry(backStackEntryOf(destination))
    }

    // Like Jetpack navigation, a screen pushed above dialogs pops them, so back doesn't return to them
    private fun VoyagerHavHostController.pushEntry(entry: VoyagerBackStackEntry) {
        if (!entry.isDialog) {
            while (size > 1 && lastItem.isDialog) pop()
        }
        push(entry)
    }

    // Likewise a screen replacing the current destination pops the dialogs below it
    private fun VoyagerHavHostController.replaceEntry(entry: VoyagerBackStackEntry) {
        val keptIndex = if (entry.isDialog) items.lastIndex - 1 else items.dropLast(1).indexOfLast { !it.isDialog }
        when (keptIndex) {
            items.lastIndex - 1 -> replace(entry)
            -1 -> replaceAll(entry)
            else -> {
                val keptItem = items[keptIndex]
                popUntil { it === keptItem }
                push(entry)
            }
        }
    }

    private val Screen.isDialog: Boolean
        get() = dialogPropertiesOf(destination) != null

    private inline fun doIfNavControllerAvailableOrThrow(action: VoyagerHavHostController.() -> Unit) {
        navHostController?.action() ?: error("No Voyager navigation host")
    }
}

/**
 * Voyager saves its back stack with Java serialization, so the entry keeps its destination as JSON and
 * [VoyagerNavigator.destinationOf] decodes it again after the back stack is restored.
 *
 * Voyager keys the state, lifecycle and disposal of a screen with [key], so [id] keeps equal destinations (e.g. the
 * same `data object` pushed twice) apart.
 */
@OptIn(ExperimentalUuidApi::class)
@Stable
internal class VoyagerBackStackEntry(
    internal val name: String,
    internal val encodedDestination: String,
    destination: ScreenDestination,
    private val id: String = Uuid.random().toString(),
) : Screen {

    @Transient
    internal var destination: ScreenDestination? = destination

    @Transient
    private var resultFlow: MutableStateFlow<Any?>? = null

    internal val result: MutableStateFlow<Any?>
        get() = resultFlow ?: MutableStateFlow<Any?>(null).also { resultFlow = it }

    override val key: ScreenKey = "$name:$id"

    @Composable
    override fun Content() {
        checkNotNull(destination).Content()
    }
}
