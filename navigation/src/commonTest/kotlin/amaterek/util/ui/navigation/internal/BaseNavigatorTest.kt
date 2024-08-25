package amaterek.util.ui.navigation.internal

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.ControlDestination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class BaseNavigatorTest {

    private val testGraph = listOf(
        mockk<GraphDestination>(),
    )

    private lateinit var subject: TestBaseNavigator
    private lateinit var subjectWithoutParent: TestBaseNavigator
    private lateinit var parent: Navigator

    @BeforeTest
    fun setUp() {
        parent = mockk()
        subject = spyk(
            TestBaseNavigator(
                graph = testGraph.toSet(),
                parent = parent,
            )
        )
        subjectWithoutParent = spyk(
            TestBaseNavigator(
                graph = testGraph.toSet(),
                parent = null,
            )
        )
    }

    // Back
    @Test
    fun WHEN_navigates_to_PreviousDestination_and_can_navigate_back_THEN_does_navigate_back() {
        every { subject.backStack.size } returns 2
        every { subject.doNavigateBack() } returns Unit

        subject.navigateTo(PreviousDestination)

        verify(exactly = 1) { subject.doNavigateBack() }
    }

    @Test
    fun WHEN_navigates_to_PreviousDestination_and_can_not_navigate_back_THEN_redirects_it_to_parent() {
        every { subject.backStack.size } returns 1
        every { parent.navigateTo(PreviousDestination) } returns Unit

        subject.navigateTo(PreviousDestination)

        verify(exactly = 1) { parent.navigateTo(PreviousDestination) }
    }

    @Test
    fun WHEN_navigates_to_PreviousDestination_and_can_not_navigate_back_and_there_is_no_parent_THEN_throw_an_error() {
        every { subject.backStack.size } returns 1

        assertFailsWith<IllegalStateException> {
            subjectWithoutParent.navigateTo(PreviousDestination)
        }
    }
}

@OptIn(InternalNavigation::class)
class TestBaseNavigator(
    graph: Set<GraphDestination>,
    parent: Navigator?,
) : BaseNavigator(graph, parent) {

    override val backStack: Navigator.BackStack
        get() = requireMockError()

    @InternalNavigation
    override fun getResultFlow(destination: ScreenDestination): Flow<Any?> {
        requireMockError()
    }

    override fun setResult(result: Any?) {
        requireMockError()
    }

    public override fun doNavigateBack() {
        requireMockError()
    }

    public override fun doPopUpTo(popUpTo: ControlDestination.PopUpTo) {
        requireMockError()
    }

    public override fun doPush(destination: ScreenDestination) {
        requireMockError()
    }

    private fun requireMockError(): Nothing = error("Requires to be mocked")
}