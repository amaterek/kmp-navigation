package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.common.InstanceDestination
import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.destination.ControlDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Composable
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("MagicNumber", "TooManyFunctions")
abstract class CommonNavigationTest(
    rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: Set<GraphDestination>,
        parent: Navigator?,
    ) -> Navigator,
) : BaseNavigationTest(rememberNavigator) {

    @Test
    fun displaysStartObjectDestination() = test {
        expectStartDestination(ObjectDestination1)
    }

    @Test
    open fun displaysStartInstanceDestination() = test {
        expectStartDestination(InstanceDestination("instance"))
    }

    @Test
    fun navigateTo() = test {
        expectStartDestination(ObjectDestination1)

        expectDestination(ObjectDestination2) { navigateTo(it) }
        val instanceDestination = InstanceDestination("navigateTo")
        expectDestination(instanceDestination) { navigateTo(it) }
        ObjectDestination1.expectLastIndexInBackStack(0)
        ObjectDestination2.expectLastIndexInBackStack(1)
        instanceDestination.expectLastIndexInBackStack(2)
    }

    @Test
    fun navigateBack() = test {
        expectStartDestination(ObjectDestination1)
        runNavigator { assertFalse(canNavigateBack()) }

        expectDestination(ObjectDestination2) { navigateTo(it) }
        runNavigator { assertTrue(canNavigateBack()) }
        expectDestination(InstanceDestination("navigateBack")) { navigateTo(it) }
        runNavigator { assertTrue(canNavigateBack()) }
        expectDestination(ObjectDestination2) { navigateBack() }
        runNavigator { assertTrue(canNavigateBack()) }
        expectDestination(ObjectDestination1) { navigateBack() }
        runNavigator { assertFalse(canNavigateBack()) }

        // If can't navigate back then PreviousDestination is redirected to parent
        assertNull(parentNavigator.lastDestination)
        runNavigator { navigateBack() }
        assertEquals(PreviousDestination, parentNavigator.lastDestination)

        parentNavigator.reset()
    }

    @Test
    fun navigateBackWithResult() = test {
        expectStartDestination(ObjectDestination1)
        expectDestination(ObjectDestination2) { navigateTo(it) }

        runNavigator { navigateBackWithResult(ObjectDestination2.result) }
        expectDestination(ObjectDestination1, result = ObjectDestination2.result)
        ObjectDestination2.expectLastIndexInBackStack(-1)

        val instanceDestination = InstanceDestination("navigate back with result")
        expectDestination(instanceDestination) { navigateTo(it) }
        runNavigator { navigateBackWithResult(instanceDestination.result) }
        expectDestination(ObjectDestination1, result = instanceDestination.result)
        instanceDestination.expectLastIndexInBackStack(-1)

        // If can't navigate back then PreviousDestination with result is redirected to parent
        val result = "to parent result"
        assertNull(parentNavigator.lastDestination)
        runNavigator { navigateBackWithResult(result) }
        assertEquals(ControlDestination.WithResult(PreviousDestination, result), parentNavigator.lastDestination)

        parentNavigator.reset()
    }

    @Test
    fun replace() = test {
        expectStartDestination(ObjectDestination1)

        expectDestination(ObjectDestination2) { replace(it) }
        ObjectDestination1.expectLastIndexInBackStack(-1)
        ObjectDestination2.expectLastIndexInBackStack(0)

        val instanceDestination = InstanceDestination("replace")
        expectDestination(instanceDestination) { replace(it) }
        ObjectDestination1.expectLastIndexInBackStack(-1)
        ObjectDestination2.expectLastIndexInBackStack(-1)
        instanceDestination.expectLastIndexInBackStack(0)
    }

    @Test
    fun replaceAll() = test {
        expectStartDestination(ObjectDestination1)

        expectDestination(ObjectDestination2) { replaceAll(it) }
        ObjectDestination2.expectLastIndexInBackStack(0)

        expectDestination(ObjectDestination2) { navigateTo(it) }
        expectDestination(ObjectDestination2) { navigateTo(it) }
        expectDestination(ObjectDestination1) { replaceAll(it) }
        ObjectDestination1.expectLastIndexInBackStack(0)

        expectDestination(ObjectDestination1) { navigateTo(it) }
        expectDestination(InstanceDestination("instance")) { navigateTo(it) }
        val instanceDestination = InstanceDestination("replace all")
        expectDestination(instanceDestination) { replaceAll(it) }
        instanceDestination.expectLastIndexInBackStack(0)
    }

    @Test
    fun popUpToFirst() = test {
        expectStartDestination(ObjectDestination1)

        runNavigator { popUpToFirst() }
        ObjectDestination1.expectLastIndexInBackStack(0)

        expectDestination(ObjectDestination1) { navigateTo(it) }
        expectDestination(ObjectDestination1) { navigateTo(it) }
        runNavigator { popUpToFirst() }
        ObjectDestination1.expectLastIndexInBackStack(0)

        expectDestination(ObjectDestination2) { navigateTo(it) }
        expectDestination(InstanceDestination("instance")) { navigateTo(it) }
        runNavigator { popUpToFirst() }
        ObjectDestination1.expectLastIndexInBackStack(0)

        val instanceDestination = InstanceDestination("pop up to first")
        expectDestination(instanceDestination) { replaceAll(it) }
        expectDestination(ObjectDestination1) { navigateTo(it) }
        expectDestination(ObjectDestination1) { navigateTo(it) }
        runNavigator { popUpToFirst() }
        instanceDestination.expectLastIndexInBackStack(0)
    }

    @Test
    fun popUpToInstance() = test {
        expectStartDestination(ObjectDestination1)

        runNavigator { popUpTo(ObjectDestination1) }
        ObjectDestination1.expectLastIndexInBackStack(0)

        expectDestination(ObjectDestination1) { navigateTo(it) }
        ObjectDestination1.expectLastIndexInBackStack(1)

        runNavigator { popUpTo(ObjectDestination1, inclusive = true) }
        ObjectDestination1.expectLastIndexInBackStack(0)

        val instanceDestination1 = InstanceDestination("1")
        val instanceDestination2 = InstanceDestination("2")
        val instanceDestination3 = InstanceDestination("3")

        expectDestination(instanceDestination1) { navigateTo(it) }
        expectDestination(instanceDestination2) { navigateTo(it) }
        expectDestination(instanceDestination3) { navigateTo(it) }

        // Pop up to non existing destination
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(ObjectDestination2) }
        }
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(InstanceDestination("4"), inclusive = true) }
        }

        runNavigator { popUpTo(instanceDestination3) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(3)

        runNavigator { popUpTo(instanceDestination3, inclusive = true) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(-1)

        runNavigator { popUpTo(instanceDestination1) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(-1)
        instanceDestination3.expectLastIndexInBackStack(-1)

        runNavigator { popUpTo(instanceDestination1, inclusive = true) }
        instanceDestination1.expectLastIndexInBackStack(-1)
    }

    @Test
    fun popUpToClass() = test {
        expectStartDestination(ObjectDestination1)

        runNavigator { popUpTo(ObjectDestination1::class) }
        ObjectDestination1.expectLastIndexInBackStack(0)

        expectDestination(ObjectDestination1) { navigateTo(it) }
        ObjectDestination1.expectLastIndexInBackStack(1)

        runNavigator { popUpTo(ObjectDestination1::class, inclusive = true) }
        ObjectDestination1.expectLastIndexInBackStack(0)

        val instanceDestination1 = InstanceDestination("1")
        val instanceDestination2 = InstanceDestination("2")
        val instanceDestination3 = InstanceDestination("3")

        expectDestination(instanceDestination1) { navigateTo(it) }
        expectDestination(instanceDestination2) { navigateTo(it) }
        expectDestination(instanceDestination3) { navigateTo(it) }

        // Pop up to non existing destination
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(ObjectDestination2::class) }
        }
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(ObjectDestination2::class, inclusive = true) }
        }

        runNavigator { popUpTo(InstanceDestination::class) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(3)

        runNavigator { popUpTo(InstanceDestination::class, inclusive = true) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(-1)

        runNavigator { popUpTo(ObjectDestination1::class) }
        instanceDestination1.expectLastIndexInBackStack(-1)
        instanceDestination2.expectLastIndexInBackStack(-1)
        instanceDestination3.expectLastIndexInBackStack(-1)
    }

    @Test
    fun popUpToInstanceWithReplace() = test {
        expectStartDestination(ObjectDestination1)

        runNavigator { popUpTo(ObjectDestination1, replaceWith = ObjectDestination2) }
        ObjectDestination1.expectLastIndexInBackStack(0)
        ObjectDestination2.expectLastIndexInBackStack(1)

        runNavigator { popUpTo(ObjectDestination1, replaceWith = ObjectDestination2, inclusive = true) }
        ObjectDestination1.expectLastIndexInBackStack(-1)
        ObjectDestination2.expectLastIndexInBackStack(0)

        val instanceDestination1 = InstanceDestination("1")
        val instanceDestination2 = InstanceDestination("2")
        val instanceDestination3 = InstanceDestination("3")

        expectDestination(instanceDestination1) { navigateTo(it) }
        expectDestination(instanceDestination2) { navigateTo(it) }

        // Pop up to non existing destination
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(ObjectDestination1, replaceWith = ObjectDestination2) }
        }
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(instanceDestination3, replaceWith = ObjectDestination1, inclusive = true) }
        }

        runNavigator { popUpTo(instanceDestination2, replaceWith = instanceDestination3) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(3)

        runNavigator { popUpTo(instanceDestination3, replaceWith = ObjectDestination1, inclusive = true) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(-1)
        ObjectDestination1.expectLastIndexInBackStack(3)

        runNavigator { popUpTo(instanceDestination1, replaceWith = ObjectDestination2) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(-1)
        instanceDestination3.expectLastIndexInBackStack(-1)
        ObjectDestination2.expectLastIndexInBackStack(2)
    }

    @Test
    fun popUpToClassWithReplace() = test {
        expectStartDestination(ObjectDestination1)

        runNavigator { popUpTo(ObjectDestination1::class, replaceWith = ObjectDestination2) }
        ObjectDestination1.expectLastIndexInBackStack(0)
        ObjectDestination2.expectLastIndexInBackStack(1)

        runNavigator { popUpTo(ObjectDestination1::class, replaceWith = ObjectDestination2, inclusive = true) }
        ObjectDestination1.expectLastIndexInBackStack(-1)
        ObjectDestination2.expectLastIndexInBackStack(0)

        val instanceDestination1 = InstanceDestination("1")
        val instanceDestination2 = InstanceDestination("2")
        val instanceDestination3 = InstanceDestination("3")

        expectDestination(instanceDestination1) { navigateTo(it) }
        expectDestination(instanceDestination2) { navigateTo(it) }
        expectDestination(ObjectDestination2) { navigateTo(it) }

        // Pop up to non existing destination
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(ObjectDestination1::class, replaceWith = ObjectDestination2) }
        }
        assertFailsWith<IllegalStateException> {
            runNavigator { popUpTo(ObjectDestination1::class, replaceWith = ObjectDestination1, inclusive = true) }
        }

        runNavigator { popUpTo(InstanceDestination::class, replaceWith = instanceDestination3) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(3)

        runNavigator { popUpTo(InstanceDestination::class, replaceWith = ObjectDestination1, inclusive = true) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(-1)
        ObjectDestination1.expectLastIndexInBackStack(3)

        runNavigator { popUpTo(InstanceDestination::class, replaceWith = ObjectDestination1) }
        instanceDestination1.expectLastIndexInBackStack(1)
        instanceDestination2.expectLastIndexInBackStack(2)
        instanceDestination3.expectLastIndexInBackStack(-1)
        ObjectDestination1.expectLastIndexInBackStack(3)

        runNavigator { popUpTo(ObjectDestination2::class, replaceWith = ObjectDestination1) }
        instanceDestination1.expectLastIndexInBackStack(-1)
        instanceDestination2.expectLastIndexInBackStack(-1)
        instanceDestination3.expectLastIndexInBackStack(-1)
        ObjectDestination2.expectLastIndexInBackStack(0)
        ObjectDestination1.expectLastIndexInBackStack(1)
    }

    @Test
    fun popUpWithResult() = test {
        expectStartDestination(ObjectDestination1)
        expectDestination(ObjectDestination2) { navigateTo(it) }

        runNavigator { popUpToWithResult(ObjectDestination2, ObjectDestination2.result) }
        expectDestination(ObjectDestination2, result = ObjectDestination2.result)
        ObjectDestination2.expectLastIndexInBackStack(1)

        runNavigator { popUpToWithResult(ObjectDestination2, ObjectDestination2.result, inclusive = true) }
        expectDestination(ObjectDestination1, result = ObjectDestination2.result)
        ObjectDestination2.expectLastIndexInBackStack(-1)
    }

    @Test
    fun popUpWithFirstResult() = test {
        expectStartDestination(ObjectDestination1)
        expectDestination(ObjectDestination2) { navigateTo(it) }
        val instanceDestination = InstanceDestination("pop up to first wit result")
        expectDestination(instanceDestination) { navigateTo(it) }

        runNavigator { popUpToFirstWithResult(instanceDestination.result) }
        expectDestination(ObjectDestination1, result = instanceDestination.result)
        ObjectDestination1.expectLastIndexInBackStack(0)
    }
}