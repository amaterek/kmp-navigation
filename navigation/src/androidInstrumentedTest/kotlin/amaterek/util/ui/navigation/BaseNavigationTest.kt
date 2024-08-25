package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.common.DestinationNameTag
import amaterek.util.ui.navigation.common.DestinationResultTag
import amaterek.util.ui.navigation.common.InstanceDestination
import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.common.TestDestination
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.JetpackNavigator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.coroutines.flow.Flow
import org.junit.Before
import org.junit.Rule
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class BaseNavigationTest(
    private val rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: Set<GraphDestination>,
        parent: Navigator?,
    ) -> Navigator,
) {
    @get:Rule
    val composeTestRule = createComposeRule()

    protected val parentNavigator = TestParentNavigator()

    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        parentNavigator.reset()
    }

    protected fun expectStartDestination(startDestination: TestDestination) {
        with(composeTestRule) {
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
        with(composeTestRule) {
            action?.let { runNavigator { action(destination) } }
            waitForIdle()
            onNodeWithTag(DestinationNameTag).assertTextEquals(destination.name)
            onNodeWithTag(DestinationResultTag).assertTextEquals(result)
        }
    }

    protected fun runNavigator(action: Navigator.() -> Unit) {
        composeTestRule.runOnUiThread {
            navigator.action()
        }
    }

    protected fun TestDestination.expectLastIndexInBackStack(index: Int) {
        assertEquals(index, navigator.backStack.lastIndexOf(this))
        if (this::class.objectInstance != null) {
            assertEquals(index, navigator.backStack.lastIndexOf(this::class))
        }
    }

    protected fun test(test: () -> Unit) {
        test()
        assertNull(parentNavigator.lastDestination)
    }

    @OptIn(InternalNavigation::class)
    @Composable
    private fun TestContent(startDestination: ScreenDestination) {
        MaterialTheme {
            navigator = rememberNavigator(
                startDestination,
                setOf(ObjectDestination1::class, ObjectDestination2::class, InstanceDestination::class),
                parentNavigator,
            )
            when (navigator) {
                is JetpackNavigator -> JetpackNavigationHost(navigator)
                is VoyagerNavigator -> VoyagerNavigationHost(navigator)
                else -> error("Unknown navigator")
            }
        }
    }

    class TestParentNavigator : Navigator {
        var lastDestination: Destination? = null

        override val backStack: Navigator.BackStack
            get() = error("Not supported")

        @InternalNavigation
        override fun getResultFlowForDestination(destination: ScreenDestination): Flow<Any?> {
            error("Not supported")
        }

        override fun setResult(result: Any?) {
            error("Not supported")
        }

        override fun navigateTo(destination: Destination) {
            lastDestination = destination
        }

        fun reset() {
            lastDestination = null
        }
    }
}