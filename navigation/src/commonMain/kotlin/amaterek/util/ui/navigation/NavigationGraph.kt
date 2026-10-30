package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Immutable
import androidx.compose.ui.window.DialogProperties
import kotlin.reflect.KClass

/**
 * The destinations the navigator can navigate to. Use [navigationGraph] to create it.
 */
@OptIn(InternalNavigation::class)
@Immutable
class NavigationGraph internal constructor(
    @property:InternalNavigation val entries: Map<GraphDestination, Entry>,
) {

    val destinations: Set<GraphDestination>
        get() = entries.keys

    @InternalNavigation
    data class Entry(val dialogProperties: DialogProperties?)
}

/**
 * Creates [NavigationGraph], e.g.:
 * ```
 * val MainNavigationGraph = navigationGraph {
 *     screen<SplashDestination>()
 *     screen<MainDestination>()
 *     dialog<ConfirmDialog>(properties = DialogProperties(usePlatformDefaultWidth = false))
 *     include(TasksGraph)
 * }
 * ```
 */
fun navigationGraph(builder: NavigationGraphBuilder.() -> Unit): NavigationGraph =
    NavigationGraphBuilder().apply(builder).build()

@OptIn(InternalNavigation::class)
class NavigationGraphBuilder internal constructor() {

    private val entries = LinkedHashMap<GraphDestination, NavigationGraph.Entry>()

    /**
     * Adds the screen destination to the graph.
     */
    inline fun <reified T : ScreenDestination> screen() = screen(T::class)

    /**
     * Adds the screen destination to the graph when its class is known only at runtime.
     * Dialog destinations must be added with [dialog], otherwise they are displayed as screens.
     */
    fun screen(destinationClass: GraphDestination) =
        add(destinationClass, NavigationGraph.Entry(dialogProperties = null))

    /**
     * Adds the dialog destination to the graph.
     */
    inline fun <reified T : DialogDestination> dialog(properties: DialogProperties = DialogProperties()) =
        dialog(T::class, properties)

    /**
     * Adds the dialog destination to the graph when its class is known only at runtime.
     */
    fun dialog(destinationClass: KClass<out DialogDestination>, properties: DialogProperties = DialogProperties()) =
        add(destinationClass, NavigationGraph.Entry(dialogProperties = properties))

    /**
     * Adds the destinations of a feature's graph, so the feature can keep its inner destinations internal.
     */
    fun include(graph: NavigationGraph) {
        graph.entries.forEach { (destinationClass, entry) -> add(destinationClass, entry) }
    }

    internal fun build() = NavigationGraph(entries.toMap())

    private fun add(destinationClass: GraphDestination, entry: NavigationGraph.Entry) {
        val existingEntry = entries[destinationClass]
        require(existingEntry == null || existingEntry == entry) {
            "$destinationClass is already in the graph as $existingEntry"
        }
        entries[destinationClass] = entry
    }
}
