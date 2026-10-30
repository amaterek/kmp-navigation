package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VoyagerNavigationTest : CommonNavigationTest(
    rememberNavigator = { startDestination, graph, parent ->
        rememberVoyagerNavigator(startDestination, graph, parent, NoneScreenTransition)
    }
) {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun providesStartBackStackBeforeHostIsComposed() = runComposeUiTest {
        lateinit var navigator: Navigator
        setContent {
            navigator = rememberVoyagerNavigator(
                startBackStack = listOf(ObjectDestination1, ObjectDestination2),
                graph = navigationGraph {
                    screen<ObjectDestination1>()
                    screen<ObjectDestination2>()
                },
                parent = null,
            )
        }

        runOnIdle {
            assertEquals(2, navigator.backStack.size)
            assertEquals(1, navigator.backStack.lastIndexOf(ObjectDestination2))
            assertEquals(ObjectDestination2, navigator.backStack.currentDestinationFlow.value)
            assertTrue(navigator.canNavigateBack())
            // Not redirected to the parent navigator as if the back stack was empty
            assertFailsWith<IllegalStateException> { navigator.navigateBack() }
        }
    }
}
