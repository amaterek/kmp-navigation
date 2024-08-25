package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.GraphDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface Navigator {

    interface BackStack {

        val size: Int

        val currentDestinationFlow: StateFlow<ScreenDestination>

        fun lastIndexOf(destination: ScreenDestination): Int

        fun lastIndexOf(destination: GraphDestination): Int
    }

    val backStack: BackStack

    @InternalNavigation
    fun getResultFlow(destination: ScreenDestination): Flow<Any?>

    @InternalNavigation
    fun setResult(result: Any?)

    /** Navigates to the destination
     *
     * @throws IllegalStateException if pop up to destination is not in back stack.
     */
    fun navigateTo(destination: Destination)
}
