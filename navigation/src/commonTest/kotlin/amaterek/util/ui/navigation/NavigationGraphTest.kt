package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@OptIn(InternalNavigation::class)
class NavigationGraphTest {

    @Test
    fun includesDestinationsOfOtherGraph() {
        val featureGraph = navigationGraph {
            dialog<TestDialogDestination>(properties = testDialogProperties)
        }

        val graph = navigationGraph {
            screen<TestScreenDestination>()
            include(featureGraph)
        }

        assertEquals(setOf(TestScreenDestination::class, TestDialogDestination::class), graph.destinations)
        assertNull(graph.entries.getValue(TestScreenDestination::class).dialogProperties)
        assertEquals(testDialogProperties, graph.entries.getValue(TestDialogDestination::class).dialogProperties)
    }

    @Test
    fun acceptsDestinationAddedTwiceWithSameEntry() {
        val featureGraph = navigationGraph {
            dialog<TestDialogDestination>(properties = testDialogProperties)
        }

        val graph = navigationGraph {
            dialog<TestDialogDestination>(properties = testDialogProperties)
            include(featureGraph)
        }

        assertEquals(setOf(TestDialogDestination::class), graph.destinations)
        assertEquals(testDialogProperties, graph.entries.getValue(TestDialogDestination::class).dialogProperties)
    }

    @Test
    fun rejectsDialogAddedWithOtherProperties() {
        val featureGraph = navigationGraph {
            dialog<TestDialogDestination>(properties = testDialogProperties)
        }

        assertFailsWith<IllegalArgumentException> {
            navigationGraph {
                dialog<TestDialogDestination>()
                include(featureGraph)
            }
        }
    }
}

private val testDialogProperties = DialogProperties(usePlatformDefaultWidth = false)

@Serializable
private data object TestScreenDestination : ScreenDestination {

    @Composable
    override fun Content() = Unit
}

@Serializable
private data object TestDialogDestination : DialogDestination {

    @Composable
    override fun Content() = Unit
}
