package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.common.DestinationNameTag
import amaterek.util.ui.navigation.common.InstanceDestination
import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.nav3.Nav3NavigationHost
import amaterek.util.ui.navigation.nav3.rememberNav3Navigator
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class Nav3SceneTest {

    private lateinit var navigator: Navigator

    @Test
    fun showsListAndDetailTogether() = test {
        navigate { navigateTo(ObjectDestination2) }

        expectShownDestinations(ObjectDestination1.name, ObjectDestination2.name)
    }

    @Test
    fun backPopsDetail() = test {
        navigate { navigateTo(ObjectDestination2) }

        navigate { navigateBack() }

        expectShownDestinations(ObjectDestination1.name)
    }

    @Test
    fun showsDestinationWithoutPaneAlone() = test {
        navigate { navigateTo(ObjectDestination2) }

        val destination = InstanceDestination("alone")
        navigate { navigateTo(destination) }

        expectShownDestinations(destination.name)
    }

    private fun test(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent {
            MaterialTheme {
                navigator = rememberNav3Navigator(
                    startDestination = ObjectDestination1,
                    graph = navigationGraph {
                        screen<ObjectDestination1>(metadata = TwoPaneSceneStrategy.listPane())
                        screen<ObjectDestination2>(metadata = TwoPaneSceneStrategy.detailPane())
                        screen<InstanceDestination>()
                    },
                    parent = null,
                    defaultTransition = NoneScreenTransition,
                )
                Nav3NavigationHost(navigator, sceneStrategies = listOf(TwoPaneSceneStrategy<Any>()))
            }
        }
        block()
    }

    private fun ComposeUiTest.navigate(action: Navigator.() -> Unit) {
        runOnUiThread { navigator.action() }
        waitForIdle()
    }

    private fun ComposeUiTest.expectShownDestinations(vararg names: String) {
        val shownNames = onAllNodesWithTag(DestinationNameTag)
            .fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.Text].joinToString { it.text } }
        assertEquals(names.toList(), shownNames)
    }
}

private class TwoPaneSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val detail = entries.lastOrNull()?.takeIf { DETAIL_PANE in it.metadata }
        val list = entries.getOrNull(entries.lastIndex - 1)?.takeIf { LIST_PANE in it.metadata }
        return if (list != null && detail != null) {
            TwoPaneScene(list, detail, previousEntries = entries.dropLast(1))
        } else {
            null
        }
    }

    companion object {
        private const val LIST_PANE = "TwoPaneList"
        private const val DETAIL_PANE = "TwoPaneDetail"

        fun listPane(): Map<String, Any> = mapOf(LIST_PANE to true)

        fun detailPane(): Map<String, Any> = mapOf(DETAIL_PANE to true)
    }
}

private data class TwoPaneScene<T : Any>(
    val list: NavEntry<T>,
    val detail: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
) : Scene<T> {

    override val key: Any = list.contentKey

    override val entries: List<NavEntry<T>> = listOf(list, detail)

    override val content: @Composable () -> Unit = {
        Row {
            Box(modifier = Modifier.weight(1f)) { list.Content() }
            Box(modifier = Modifier.weight(1f)) { detail.Content() }
        }
    }
}
