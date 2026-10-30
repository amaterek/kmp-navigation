package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.common.DestinationNameTag
import amaterek.util.ui.navigation.common.DestinationResultTag
import amaterek.util.ui.navigation.common.InstanceDestination
import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.common.TestDestination
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.JetpackNavigator
import amaterek.util.ui.navigation.nav3.Nav3NavigationHost
import amaterek.util.ui.navigation.nav3.Nav3Navigator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.flow.Flow
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
abstract class BaseNavigationTest(
    private val rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: NavigationGraph,
        parent: Navigator?,
    ) -> Navigator,
) {
    protected val parentNavigator = TestParentNavigator()

    private lateinit var composeUiTest: ComposeUiTest

    private lateinit var navigator: Navigator

    @BeforeTest
    fun setup() {
        parentNavigator.reset()
    }

    protected fun expectStartDestination(startDestination: TestDestination) {
        with(composeUiTest) {
            setContent { TestContent(startDestination = startDestination) }
            waitForIdle()
            onNodeWithTag(DestinationNameTag).assertTextEquals(startDestination.name)
        }
        startDestination.expectLastIndexInBackStack(0)
    }

    protected fun expectDestination(
        destination: TestDestination,
        result: String = "",
        action: (Navigator.(TestDestination) -> Unit)? = null,
    ) {
        with(composeUiTest) {
            action?.let { runNavigator { action(destination) } }
            waitForIdle()
            onNodeWithTag(DestinationNameTag).assertTextEquals(destination.name)
            onNodeWithTag(DestinationResultTag).assertTextEquals(result)
        }
    }

    protected fun runNavigator(action: Navigator.() -> Unit) {
        composeUiTest.runOnUiThread {
            navigator.action()
        }
    }

    protected fun TestDestination.expectLastIndexInBackStack(index: Int) {
        assertEquals(index, navigator.backStack.lastIndexOf(this))
        if (this !is InstanceDestination) {
            assertEquals(index, navigator.backStack.lastIndexOf(this::class))
        }
    }

    protected fun test(test: () -> Unit) = runComposeUiTest {
        composeUiTest = this
        test()
        assertNull(parentNavigator.lastDestination)
    }

    @OptIn(InternalNavigation::class)
    @Suppress("DEPRECATION")
    @Composable
    private fun TestContent(startDestination: ScreenDestination) {
        MaterialTheme {
            navigator = rememberNavigator(
                startDestination,
                navigationGraph {
                    screen<ObjectDestination1>()
                    screen<ObjectDestination2>()
                    screen<InstanceDestination>()
                },
                parentNavigator,
            )
            when (val navigator = navigator) {
                is JetpackNavigator -> JetpackNavigationHost(navigator)
                is Nav3Navigator -> Nav3NavigationHost(navigator)
                is VoyagerNavigator -> VoyagerNavigationHost(navigator)
                else -> error("Unknown navigator")
            }
        }
    }

    class TestParentNavigator : Navigator {
        var lastDestination: Destination? = null

        override val backStack: Navigator.BackStack
            get() = error("Not supported")

        override fun navigateTo(destination: Destination) {
            lastDestination = destination
        }

        @InternalNavigation
        override fun getResultFlow(navigationResultHandler: Any): Flow<Any?> = error("Not supported")

        @InternalNavigation
        override fun setResult(result: Any?) = error("Not supported")

        @InternalNavigation
        override fun clearResult(navigationResultHandler: Any) = error("Not supported")

        fun reset() {
            lastDestination = null
        }
    }
}
