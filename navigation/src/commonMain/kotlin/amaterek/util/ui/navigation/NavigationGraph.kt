package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Immutable
import androidx.compose.ui.window.DialogProperties
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * The destinations the navigator can navigate to. Use [navigationGraph] to create it.
 */
@OptIn(InternalNavigation::class)
@Immutable
class NavigationGraph internal constructor(
    @property:InternalNavigation val entries: Map<GraphDestination, Entry>,
    @property:InternalNavigation val serializersModule: SerializersModule,
) {

    val destinations: Set<GraphDestination>
        get() = entries.keys

    @InternalNavigation
    data class Entry(
        val serializer: KSerializer<out ScreenDestination>,
        val dialogProperties: DialogProperties?,
        val metadata: Map<String, Any>,
    ) {
        val name: String = serializer.descriptor.serialName
    }
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

    private var serializersModule = EmptySerializersModule()

    /**
     * Adds the screen destination to the graph.
     *
     * @param metadata Navigation 3 entry metadata, e.g. a pane of a scene (`ListDetailSceneStrategy.listPane()`);
     * ignored by the other navigators.
     */
    inline fun <reified T : ScreenDestination> screen(metadata: Map<String, Any> = emptyMap()) =
        screen(T::class, serializer<T>(), metadata)

    /**
     * Adds the screen destination to the graph when its class is known only at runtime.
     * The class must be annotated with [kotlinx.serialization.Serializable].
     * Dialog destinations must be added with [dialog], otherwise they are displayed as screens.
     */
    fun screen(destinationClass: GraphDestination, metadata: Map<String, Any> = emptyMap()) = add(
        destinationClass,
        NavigationGraph.Entry(destinationClass.destinationSerializer(), dialogProperties = null, metadata),
    )

    fun <T : ScreenDestination> screen(
        destinationClass: KClass<T>,
        serializer: KSerializer<T>,
        metadata: Map<String, Any> = emptyMap(),
    ) = add(destinationClass, NavigationGraph.Entry(serializer, dialogProperties = null, metadata))

    /**
     * Adds the dialog destination to the graph.
     *
     * @param metadata Navigation 3 entry metadata; ignored by the other navigators.
     */
    inline fun <reified T : DialogDestination> dialog(
        properties: DialogProperties = DialogProperties(),
        metadata: Map<String, Any> = emptyMap(),
    ) = dialog(T::class, serializer<T>(), properties, metadata)

    /**
     * Adds the dialog destination to the graph when its class is known only at runtime.
     * The class must be annotated with [kotlinx.serialization.Serializable].
     */
    fun dialog(
        destinationClass: KClass<out DialogDestination>,
        properties: DialogProperties = DialogProperties(),
        metadata: Map<String, Any> = emptyMap(),
    ) = add(destinationClass, NavigationGraph.Entry(destinationClass.destinationSerializer(), properties, metadata))

    fun <T : DialogDestination> dialog(
        destinationClass: KClass<T>,
        serializer: KSerializer<T>,
        properties: DialogProperties = DialogProperties(),
        metadata: Map<String, Any> = emptyMap(),
    ) = add(destinationClass, NavigationGraph.Entry(serializer, properties, metadata))

    /**
     * Adds serializers used by destinations, e.g. for properties with interface types.
     */
    fun serializersModule(module: SerializersModule) {
        serializersModule += module
    }

    /**
     * Adds the destinations of a feature's graph, so the feature can keep its inner destinations internal.
     */
    fun include(graph: NavigationGraph) {
        graph.entries.forEach { (destinationClass, entry) -> add(destinationClass, entry) }
        serializersModule += graph.serializersModule
    }

    internal fun build() = NavigationGraph(entries.toMap(), serializersModule)

    private fun add(destinationClass: GraphDestination, entry: NavigationGraph.Entry) {
        val existingEntry = entries[destinationClass]
        require(existingEntry == null || existingEntry == entry) {
            "$destinationClass is already in the graph as $existingEntry"
        }
        entries.entries
            .firstOrNull { (otherClass, other) -> otherClass != destinationClass && other.name == entry.name }
            ?.let { (otherClass, _) ->
                error("$destinationClass and $otherClass have the same serial name: ${entry.name}")
            }
        entries[destinationClass] = entry
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
private fun GraphDestination.destinationSerializer(): KSerializer<out ScreenDestination> =
    EmptySerializersModule().serializer(this, emptyList(), isNullable = false) as KSerializer<out ScreenDestination>
