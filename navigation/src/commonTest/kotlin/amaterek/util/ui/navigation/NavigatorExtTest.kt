@file:Suppress("WildcardImport", "NoWildcardImports")

package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination.*
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
    fun WHEN_canNavigateBack_is_called_and_backstack_entry_size_is_grater_then_1_THEN_navigates_to_PreviousDestination() {
        (2..10).onEach {
            every { navigator.backStack.size } returns it

            assertTrue(navigator.canNavigateBack())
        }
    }

    @Test
    fun WHEN_canNavigateBack_is_called_and_backstack_entry_size_is_lower_then_2_THEN_navigates_to_PreviousDestination() {
        (-10..1).onEach {
            every { navigator.backStack.size } returns it

            assertFalse(navigator.canNavigateBack())
        }
    }

    @Test
    fun WHEN_navigateBack_is_called_THEN_navigates_to_PreviousDestination() {
        test(
            on = { navigateBack() },
            expect = PreviousDestination
        )
    }

    @Test
    fun WHEN_navigateBackWithResult_is_called_THEN_navigates_to_WithResult_control_destination() {
        val result = mockk<Any>()
        test(
            on = { navigateBackWithResult(result) },
            expect = WithResult(PreviousDestination, result),
        )
    }

    @Test
    fun WHEN_replace_is_called_THEN_navigates_to_WithResult_control_destination() {
        val destination = mockk<ScreenDestination>()
        test(
            on = { replace(destination) },
            expect = PopUpTo.CurrentDestination(inclusive = true, replaceWith = destination),
        )
    }

    @Test
    fun WHEN_replaceAll_is_called_THEN_navigates_to_WithResult_control_destination() {
        val destination = mockk<ScreenDestination>()
        test(
            on = { replaceAll(destination) },
            expect = PopUpTo.FirstDestination(inclusive = true, replaceWith = destination),
        )
    }

    // PopUpTo
    @Test
    fun WHEN_popUpToFirst_is_called_THEN_navigates_to_WithResult_control_destination() {
        test(
            on = { popUpToFirst() },
            expect = PopUpTo.FirstDestination(inclusive = false, replaceWith = null),
        )
    }

    @Test
    fun WHEN_popUpToFirstWithResult_is_called_THEN_navigates_to_WithResult_control_destination() {
        val result = mockk<Any>()
        test(
            on = { popUpToFirstWithResult(result) },
            expect = WithResult(
                destination = PopUpTo.FirstDestination(inclusive = false, replaceWith = null),
                result = result,
            )
        )
    }

    @Test
    fun WHEN_popUpTo_is_called_with_GraphDestination_THEN_navigates_to_PopUpToDestination() {
        val destination = mockk<GraphDestination>()
        val inclusive = Random.nextBoolean()
        test(
            on = { popUpTo(destination, inclusive = inclusive) },
            expect = PopUpTo.DestinationClass(destination = destination, inclusive = inclusive, replaceWith = null),
        )
    }

    @Test
    fun WHEN_popUpTo_is_called_with_ScreenDestination_THEN_navigates_to_PopUpToDestination() {
        val destination = mockk<ScreenDestination>()
        val inclusive = Random.nextBoolean()
        test(
            on = { popUpTo(destination, inclusive = inclusive) },
            expect = PopUpTo.DestinationInstance(destination = destination, inclusive = inclusive, replaceWith = null),
        )
    }

    @Test
    fun WHEN_popUpToWithResult_is_called_with_GraphDestination_THEN_navigates_to_PopUpToDestination() {
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
    fun WHEN_popUpToWithResult_is_called_with_ScreenDestination_THEN_navigates_to_PopUpToDestination() {
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
    fun WHEN_resultFlow_is_called_THEN_get_result_flow_for_current_destination() {
        // TODO
    }

    private fun test(on: Navigator.() -> Unit, expect: Destination) {
        every { navigator.navigateTo(expect) } returns Unit

        navigator.on()

        verifySequence { navigator.navigateTo(expect) }
    }
}