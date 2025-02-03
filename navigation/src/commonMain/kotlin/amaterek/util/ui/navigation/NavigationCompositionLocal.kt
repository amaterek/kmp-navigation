package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.destination.ScreenDestination
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.Flow

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Local navigator hasn't been provided")
}

val LocalDestination = staticCompositionLocalOf<ScreenDestination> {
    error("Local destination hasn't been provided")
}

val LocalNavigationResultFlow = staticCompositionLocalOf<Flow<Any>> {
    error("Local navigation result hasn't been provided")
}
