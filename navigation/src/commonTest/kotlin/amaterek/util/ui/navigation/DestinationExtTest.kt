package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.NoneDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.redirectToParentIfNotInGraph
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(InternalNavigation::class)
class DestinationExtTest {

    @Test
    fun redirectToParentIfNotInGraphWrapsDestination() {
        val destination = PreviousDestination.redirectToParentIfNotInGraph()

        assertEquals(PreviousDestination, (destination as NavigatorDestination.RedirectToParent).destination)
    }

    @Test
    fun redirectToParentIfNotInGraphKeepsNoneDestination() {
        assertSame(NoneDestination, NoneDestination.redirectToParentIfNotInGraph())
    }
}
