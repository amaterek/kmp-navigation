package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.popUpToFirst
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.nav3.Nav3NavigationHost
import amaterek.util.ui.navigation.nav3.rememberNav3Navigator
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class JetpackScreenStateTest : ScreenStateTest(
    rememberNavigator = { startDestination, graph ->
        rememberJetpackNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { JetpackNavigationHost(it) },
)

class Nav3ScreenStateTest : ScreenStateTest(
    rememberNavigator = { startDestination, graph ->
        rememberNav3Navigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { Nav3NavigationHost(it) },
)

class VoyagerScreenStateTest : ScreenStateTest(
    rememberNavigator = { startDestination, graph ->
        rememberVoyagerNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { VoyagerNavigationHost(it) },
)

@OptIn(ExperimentalTestApi::class)
abstract class ScreenStateTest(
    private val rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: NavigationGraph,
    ) -> Navigator,
    private val navigationHost: @Composable (Navigator) -> Unit,
) {

    private lateinit var navigator: Navigator

    @Test
    fun keepsStateOfDestinationInBackStack() = test {
        navigate { navigateTo(SaveableValueDestination) }
        val value = saveableValue()

        navigate { navigateTo(ObjectDestination2) }
        navigate { navigateBack() }

        assertEquals(value, saveableValue())
    }

    @Test
    fun opensSameDestinationAgainWithNewState() = test {
        navigate { navigateTo(SaveableValueDestination) }
        val value = saveableValue()

        navigate { navigateTo(ObjectDestination2) }
        navigate { navigateTo(SaveableValueDestination) }

        assertNotEquals(value, saveableValue())
    }

    @Test
    fun replacesDestinationWithSameDestinationWithNewState() = test {
        navigate { navigateTo(SaveableValueDestination) }
        val value = saveableValue()

        navigate { navigateTo(SaveableValueDestination.popUpToFirst()) }

        assertNotEquals(value, saveableValue())
    }

    private fun test(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent {
            navigator = rememberNavigator(
                ObjectDestination1,
                navigationGraph {
                    screen<ObjectDestination1>()
                    screen<ObjectDestination2>()
                    screen<SaveableValueDestination>()
                },
            )
            navigationHost(navigator)
        }
        block()
    }

    private fun ComposeUiTest.navigate(action: Navigator.() -> Unit) {
        runOnUiThread { navigator.action() }
        waitForIdle()
    }

    private fun ComposeUiTest.saveableValue(): String =
        onNodeWithTag(SAVEABLE_VALUE_TAG).fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString { it.text }
}

private const val SAVEABLE_VALUE_TAG = "SaveableValue"

private var nextSaveableValue = 0

@Serializable
private data object SaveableValueDestination : ScreenDestination {

    @Composable
    override fun Content() {
        val value = rememberSaveable { nextSaveableValue++ }
        Text(modifier = Modifier.testTag(SAVEABLE_VALUE_TAG), text = value.toString())
    }
}
