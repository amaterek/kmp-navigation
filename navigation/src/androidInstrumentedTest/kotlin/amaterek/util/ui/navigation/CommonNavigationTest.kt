package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.common.DestinationNameTag
import amaterek.util.ui.navigation.common.InstanceDestination
import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.common.TestDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.JetpackNavigator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

abstract class CommonNavigationTest(
    private val rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: Set<GraphDestination>,
        parent: Navigator?,
    ) -> Navigator,
) {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigator: Navigator

    @Test
    fun displaysStartObjectDestination() {
        testStartDestination(ObjectDestination1)
    }

    @Test
    open fun displaysStartInstanceDestination() {
        testStartDestination(InstanceDestination("instance"))
    }

    @Test
    fun navigatesToObjectDestination() {
        testStartDestination(ObjectDestination1)
        testDestination(ObjectDestination2) { navigateTo(ObjectDestination2) }
    }

    @Test
    fun navigatesToInstanceDestination() {
        testStartDestination(ObjectDestination1)
        val instanceDestination = InstanceDestination("navigateTo")
        testDestination(instanceDestination) { navigateTo(instanceDestination) }
    }

    private fun testStartDestination(startDestination: TestDestination) {
        with(composeTestRule) {
            setContent { TestContent(startDestination = startDestination) }
            waitForIdle()
            onNodeWithTag(DestinationNameTag).assertTextEquals(startDestination.name)
        }
    }

    private fun testDestination(destination: TestDestination, action: Navigator.() -> Unit) {
        with(composeTestRule) {
            runOnUiThread {
                navigator.action()
            }
            waitForIdle()
            onNodeWithTag(DestinationNameTag).assertTextEquals(destination.name)
        }
    }

    @OptIn(InternalNavigation::class)
    @Composable
    private fun TestContent(startDestination: ScreenDestination) {
        MaterialTheme {
            navigator = rememberNavigator(
                startDestination,
                setOf(ObjectDestination1::class, ObjectDestination2::class, InstanceDestination::class),
                null,
            )
            when (navigator) {
                is JetpackNavigator -> JetpackNavigationHost(navigator)
                is VoyagerNavigator -> VoyagerNavigationHost(navigator)
                else -> error("Unknown navigator")
            }
        }
    }
}