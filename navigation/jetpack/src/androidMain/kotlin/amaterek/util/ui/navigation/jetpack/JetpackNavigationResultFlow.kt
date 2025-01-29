package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.internal.NavigationResultEmitter
import amaterek.util.ui.navigation.internal.NavigationResultFlow
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.flow.Flow

@InternalNavigation
internal class JetpackNavigationResultFlow(private val navBackStackEntry: NavBackStackEntry) : NavigationResultFlow, NavigationResultEmitter {

    override val resultFlow: Flow<Any?>
        get() = navBackStackEntry.savedStateHandle.getStateFlow(ResultFlowKey, null)

    override fun emitNavigationResult(result: Any?) {
        navBackStackEntry.emitNavigationResult(result)
    }
}

@Suppress("NOTHING_TO_INLINE")
internal fun NavBackStackEntry.emitNavigationResult(result: Any?) {
    savedStateHandle[ResultFlowKey] = result
}

@Suppress("TopLevelPropertyNaming")
private const val ResultFlowKey = "navigation_result"
