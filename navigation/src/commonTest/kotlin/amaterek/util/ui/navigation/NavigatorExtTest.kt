@file:Suppress("WildcardImport", "NoWildcardImports")

package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.ControlDestination.*
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import io.mockk.*
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(InternalNavigation::class)
class NavigatorExtTest {

    private lateinit var navigator: Navigator

    @BeforeTest
    fun setUp() {
        navigator = mockk<Navigator>()
    }

    // Back
    @Test
    fun `WHEN canNavigateBack is called and backstack entry size is grater then 1 THEN navigates to PreviousDestination`() {
        (2..10).onEach {
            every { navigator.backStack.size } returns it

            assertTrue(navigator.canNavigateBack())
        }
    }

    @Test
    fun `WHEN canNavigateBack is called and backstack entry size is lower then 2 THEN navigates to PreviousDestination`() {
        (-10..1).onEach {
            every { navigator.backStack.size } returns it

            assertFalse(navigator.canNavigateBack())
        }
    }

    @Test
    fun `WHEN navigateBack is called THEN navigates to PreviousDestination`() {
        test(
            on = { navigateBack() },
            expect = PreviousDestination
        )
    }

    @Test
    fun `WHEN navigateBackWithResult is called THEN navigates to WithResult control destination`() {
        val result = mockk<Any>()
        test(
            on = { navigateBackWithResult(result) },
            expect = WithResult(PreviousDestination, result),
        )
    }

    // PopUpTo
    @Test
    fun `WHEN popUpTo is called with GraphDestination THEN navigates to PopUpToDestination`() {
        val destination = mockk<GraphDestination>()
        val inclusive = Random.nextBoolean()
        test(
            on = { popUpTo(destination, inclusive = inclusive) },
            expect = PopUpTo.DestinationClass(destination = destination, inclusive = inclusive, replaceWith = null),
        )
    }

    @Test
    fun `WHEN popUpTo is called with ScreenDestination THEN navigates to PopUpToDestination`() {
        val destination = mockk<ScreenDestination>()
        val inclusive = Random.nextBoolean()
        test(
            on = { popUpTo(destination, inclusive = inclusive) },
            expect = PopUpTo.DestinationInstance(destination = destination, inclusive = inclusive, replaceWith = null),
        )
    }

    @Test
    fun `WHEN popUpToWithResult is called with GraphDestination THEN navigates to PopUpToDestination`() {
        val result = mockk<Any>()
        val destination = mockk<GraphDestination>()
        val inclusive = Random.nextBoolean()
        test(
            on = { popUpToWithResult(destination, result, inclusive = inclusive) },
            expect = WithResult(
                destination = PopUpTo.DestinationClass(destination = destination, inclusive = inclusive, replaceWith = null),
                result = result,
            ),
        )
    }

    @Test
    fun `WHEN popUpToWithResult is called with ScreenDestination THEN navigates to PopUpToDestination`() {
        val result = mockk<Any>()
        val destination = mockk<ScreenDestination>()
        val inclusive = Random.nextBoolean()
        test(
            on = { popUpToWithResult(destination, result, inclusive = inclusive) },
            expect = WithResult(
                destination = PopUpTo.DestinationInstance(destination = destination, inclusive = inclusive, replaceWith = null),
                result = result,
            ),
        )
    }

    @Test
    fun `WHEN getResultFlowFor is called THEN get result flow for current destination`() {
        // TODO
    }

    private fun test(on: Navigator.() -> Unit, expect: Destination) {
        every { navigator.navigateTo(expect) } returns Unit

        navigator.on()

        verifySequence { navigator.navigateTo(expect) }
    }
}