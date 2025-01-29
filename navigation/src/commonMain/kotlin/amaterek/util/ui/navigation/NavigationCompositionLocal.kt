package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.internal.NavigationResultFlow
import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Local navigator hasn't been provided")
}

val LocalDestination = staticCompositionLocalOf<ScreenDestination> {
    error("Local destination hasn't been provided")
}

@InternalNavigation
val LocalNavigationResultFlow = staticCompositionLocalOf<NavigationResultFlow> {
    error("Local navigation result hasn't been provided")
}
