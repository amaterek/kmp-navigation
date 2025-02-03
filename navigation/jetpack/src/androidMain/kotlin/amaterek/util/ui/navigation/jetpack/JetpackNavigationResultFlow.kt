package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.internal.NavigationResultEmitter
import amaterek.util.ui.navigation.internal.NavigationResultFlow
import androidx.navigation.NavBackStackEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

@InternalNavigation
internal class JetpackNavigationResultFlow(private val navBackStackEntry: NavBackStackEntry) : NavigationResultFlow, NavigationResultEmitter {

    private val _resultFlow: Flow<Any?>
        get() = navBackStackEntry.savedStateHandle.getStateFlow(ResultFlowKey, null)

    override suspend fun collect(collector: FlowCollector<Any>) {
        _resultFlow
            .filterNotNull()
            .onEach { navBackStackEntry.emitNavigationResult(result = null) }
            .collect(collector)
    }

    override fun emitNavigationResult(result: Any) {
        navBackStackEntry.emitNavigationResult(result)
    }
}

internal fun NavBackStackEntry.emitNavigationResult(result: Any?) {
    savedStateHandle[ResultFlowKey] = result
}

@Suppress("TopLevelPropertyNaming")
private const val ResultFlowKey = "navigation_result"
