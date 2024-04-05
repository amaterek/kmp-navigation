package amaterek.util.ui.navigation.sample

import amaterek.util.log.Log
import amaterek.util.log.getDefaultLogger
import amaterek.util.ui.navigation.sample.ui.navigation.GetDefaultNavigationHost
import amaterek.util.ui.navigation.sample.ui.navigation.PlatformNavigation
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() {
    Log.setLogger(getDefaultLogger())

    return application(exitProcessOnExit = true) {

        val platformNavigation = remember {
            PlatformNavigation(this)
        }

        Window(
            onCloseRequest = ::exitApplication,
            state = WindowState(size = DpSize(480.dp, 800.dp)),
            title = "Navigation Sample",
        ) {
            ComposeApp(
                platformNavigation,
                GetDefaultNavigationHost(),
            )
        }
    }
}