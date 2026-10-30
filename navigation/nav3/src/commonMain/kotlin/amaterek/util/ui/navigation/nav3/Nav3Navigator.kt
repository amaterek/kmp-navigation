package amaterek.util.ui.navigation.nav3

import amaterek.util.ui.navigation.NavigationGraph
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.BaseNavigator
import amaterek.util.ui.navigation.transition.ScreenTransition
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Back stack entry of [Nav3Navigator]; [id] keeps equal destinations (e.g. the same `data object` pushed twice)
 * apart and identifies the entry's state and results.
 */
@OptIn(ExperimentalUuidApi::class)
internal data class Nav3Entry(
    val destination: ScreenDestination,
    val id: String = Uuid.random().toString(),
)

@InternalNavigation
class Nav3Navigator internal constructor(
    internal val graph: NavigationGraph,
    internal val entries: SnapshotStateList<Nav3Entry>,
    private val results: SnapshotStateMap<String, Any>,
    parent: Navigator?,
    internal val defaultTransition: ScreenTransition,
) : BaseNavigator(graph.destinations, parent) {

    // The entries are saved with the graph's serializers, so a destination outside the graph fails here instead of
    // when the app goes to the background
    init {
        entries.forEach { if (it.destination::class !in graph.destinations) destinationNotFoundInGraphError(it.destination) }
    }

    private val currentDestination = MutableStateFlow(entries.last().destination)

    override val backStack: Navigator.BackStack = object : Navigator.BackStack {

        override val size: Int
            get() = entries.size

        override val currentDestinationFlow: StateFlow<ScreenDestination> = currentDestination.asStateFlow()

        override fun lastIndexOf(destination: ScreenDestination): Int =
            entries.indexOfLast { it.destination == destination }

        override fun lastIndexOf(destination: GraphDestination): Int =
            entries.indexOfLast { it.destination::class == destination }
    }

    private val ScreenDestination.isDialog: Boolean
        get() = graph.entries[this::class]?.dialogProperties != null

    override fun getResultFlow(navigationResultHandler: Any): Flow<Any?> {
        val entryId = navigationResultHandler as String
        return snapshotFlow { results[entryId] }
    }

    override fun setResult(result: Any?) {
        val entryId = entries.last().id
        if (result == null) results.remove(entryId) else results[entryId] = result
    }

    override fun doNavigateBack() = updateBackStack {
        removeAt(lastIndex)
    }

    override fun doPush(destination: ScreenDestination) = updateBackStack {
        push(destination)
    }

    override fun doPopUpTo(popUpTo: NavigatorDestination.PopUpTo) = updateBackStack {
        val keptCount = when (popUpTo) {
            is NavigatorDestination.PopUpTo.FirstDestination -> if (popUpTo.inclusive) 0 else 1
            is NavigatorDestination.PopUpTo.CurrentDestination -> if (popUpTo.inclusive) size - 1 else size
            is NavigatorDestination.PopUpTo.DestinationInstance ->
                keptCount(backStack.lastIndexOf(popUpTo.destination), popUpTo.inclusive)

            is NavigatorDestination.PopUpTo.DestinationClass ->
                keptCount(backStack.lastIndexOf(popUpTo.destination), popUpTo.inclusive)
        }
        val replaceWith = popUpTo.replaceWith
        check(keptCount > 0 || replaceWith != null) { "Back stack can not be empty" }
        subList(keptCount, size).clear()
        replaceWith?.let { push(it) }
    }

    private fun keptCount(index: Int, inclusive: Boolean): Int = if (inclusive) index else index + 1

    // Like Jetpack navigation, a screen pushed above dialogs pops them, so back doesn't return to them
    private fun MutableList<Nav3Entry>.push(destination: ScreenDestination) {
        if (!destination.isDialog) {
            while (size > 1 && last().destination.isDialog) removeAt(lastIndex)
        }
        add(Nav3Entry(destination))
    }

    private inline fun updateBackStack(update: MutableList<Nav3Entry>.() -> Unit) {
        Snapshot.withMutableSnapshot { entries.update() }
        val ids = entries.mapTo(mutableSetOf()) { it.id }
        results.keys.retainAll(ids)
        currentDestination.value = entries.last().destination
    }
}

// The entries are saved as JSON with the serializers of the graph's destinations
@OptIn(InternalNavigation::class)
internal fun nav3EntriesSaver(graph: NavigationGraph): Saver<SnapshotStateList<Nav3Entry>, String> {
    val json = Json { serializersModule = graph.serializersModule }
    val entriesByName = graph.entries.values.associateBy { it.name }
    return Saver(
        save = { entries ->
            val savedEntries = entries.map { entry ->
                val graphEntry = graph.entries.getValue(entry.destination::class)

                @Suppress("UNCHECKED_CAST")
                val serializer = graphEntry.serializer as KSerializer<ScreenDestination>
                SavedEntry(graphEntry.name, entry.id, json.encodeToJsonElement(serializer, entry.destination))
            }
            json.encodeToString(savedEntries)
        },
        restore = { saved ->
            json.decodeFromString<List<SavedEntry>>(saved)
                .map { savedEntry ->
                    val graphEntry = entriesByName[savedEntry.name] ?: error("Unknown destination: ${savedEntry.name}")
                    Nav3Entry(json.decodeFromJsonElement(graphEntry.serializer, savedEntry.destination), savedEntry.id)
                }
                .toMutableStateList()
        },
    )
}

@Serializable
private data class SavedEntry(val name: String, val id: String, val destination: JsonElement)
