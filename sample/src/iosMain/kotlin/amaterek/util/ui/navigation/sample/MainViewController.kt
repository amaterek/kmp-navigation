package amaterek.util.ui.navigation.sample

import amaterek.util.ui.navigation.sample.ui.navigation.GetDefaultNavigationHost
import amaterek.util.ui.navigation.sample.ui.navigation.PlatformNavigation
import androidx.compose.ui.window.ComposeUIViewController

@Suppress("FunctionName")
fun MainViewController() = ComposeUIViewController {
    ComposeApp(
        PlatformNavigation(Unit), // FIXME pass correct context
        GetDefaultNavigationHost(),
    )
}