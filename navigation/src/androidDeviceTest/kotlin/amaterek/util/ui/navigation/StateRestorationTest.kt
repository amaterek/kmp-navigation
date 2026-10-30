package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.common.DestinationNameTag
import amaterek.util.ui.navigation.common.InstanceDestination
import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.common.TestDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.nav3.Nav3NavigationHost
import amaterek.util.ui.navigation.nav3.rememberNav3Navigator
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.StateRestorationTester
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

// Compose Multiplatform's StateRestorationTester isn't implemented on iOS, so the tests run only on Android
class JetpackStateRestorationTest : StateRestorationTest(
    rememberNavigator = { startDestination, graph ->
        rememberJetpackNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { JetpackNavigationHost(it) },
)

class Nav3StateRestorationTest : StateRestorationTest(
    rememberNavigator = { startDestination, graph ->
        rememberNav3Navigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { Nav3NavigationHost(it) },
)

@Suppress("DEPRECATION")
class VoyagerStateRestorationTest : StateRestorationTest(
    rememberNavigator = { startDestination, graph ->
        rememberVoyagerNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { VoyagerNavigationHost(it) },
)

@OptIn(ExperimentalTestApi::class)
abstract class StateRestorationTest(
    private val rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: NavigationGraph,
    ) -> Navigator,
    private val navigationHost: @Composable (Navigator) -> Unit,
) {

    private lateinit var stateRestorationTester: StateRestorationTester

    private lateinit var navigator: Navigator

    @Test
    fun restoresBackStack() = test {
        val instanceDestination = InstanceDestination("argument")
        navigate { navigateTo(instanceDestination) }
        navigate { navigateTo(ObjectDestination2) }

        stateRestorationTester.emulateSaveAndRestore()

        expectDestination(ObjectDestination2)
        assertEquals(1, navigator.backStack.lastIndexOf(instanceDestination))
        navigate { navigateBack() }
        expectDestination(instanceDestination)
        navigate { navigateBack() }
        expectDestination(ObjectDestination1)
    }

    @Test
    fun restoresSaveableStateOfVisibleScreen() = test {
        navigate { navigateTo(SaveableDestination) }
        val savedValue = saveableValue()

        stateRestorationTester.emulateSaveAndRestore()

        expectDestination(SaveableDestination)
        onNodeWithTag(SAVEABLE_VALUE_TAG).assertTextEquals(savedValue)
    }

    @Test
    fun restoresSaveableStateOfScreenInBackStack() = test {
        navigate { navigateTo(SaveableDestination) }
        val savedValue = saveableValue()
        navigate { navigateTo(ObjectDestination2) }

        stateRestorationTester.emulateSaveAndRestore()
        navigate { navigateBack() }

        expectDestination(SaveableDestination)
        onNodeWithTag(SAVEABLE_VALUE_TAG).assertTextEquals(savedValue)
    }

    private fun test(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        stateRestorationTester = StateRestorationTester(this)
        stateRestorationTester.setContent {
            MaterialTheme {
                navigator = rememberNavigator(
                    ObjectDestination1,
                    navigationGraph {
                        screen<ObjectDestination1>()
                        screen<ObjectDestination2>()
                        screen<InstanceDestination>()
                        screen<SaveableDestination>()
                    },
                )
                navigationHost(navigator)
            }
        }
        block()
    }

    private fun ComposeUiTest.navigate(action: Navigator.() -> Unit) {
        runOnUiThread { navigator.action() }
        waitForIdle()
    }

    private fun ComposeUiTest.expectDestination(destination: TestDestination) {
        waitForIdle()
        onNodeWithTag(DestinationNameTag).assertTextEquals(destination.name)
    }

    private fun ComposeUiTest.saveableValue(): String {
        expectDestination(SaveableDestination)
        return onNodeWithTag(SAVEABLE_VALUE_TAG).fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .joinToString { it.text }
    }
}

private const val SAVEABLE_VALUE_TAG = "SaveableValue"

private var nextSaveableValue = 0

@Serializable
private data object SaveableDestination : TestDestination {

    override val name = "SaveableDestination"

    @Composable
    override fun Content() {
        val value = rememberSaveable { nextSaveableValue++ }
        Column {
            Text(modifier = Modifier.testTag(DestinationNameTag), text = name)
            Text(modifier = Modifier.testTag(SAVEABLE_VALUE_TAG), text = value.toString())
        }
    }
}
