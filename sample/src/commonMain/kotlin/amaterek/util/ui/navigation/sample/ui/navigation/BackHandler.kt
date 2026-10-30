package amaterek.util.ui.navigation.sample.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 * Calls [onBack] on system back (Android back button or gesture, iOS back swipe, Escape on desktop) instead of the
 * navigation host.
 */
@Composable
internal fun BackHandler(onBack: () -> Unit) {
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        onBackCompleted = onBack,
    )
}
