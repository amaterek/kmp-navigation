package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.ui.navigation.LocalDestination
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable

@NonRestartableComposable
@Composable
fun LifecycleLogger() {
    PlatformLifecycleLogger(LocalDestination.current::class.simpleName)
}

@Composable
internal expect fun PlatformLifecycleLogger(name: String?)