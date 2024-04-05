package amaterek.util.ui.navigation.backhandler

import amaterek.util.ui.navigation.backhandler.platform.PlatformBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable

@Composable
@NonRestartableComposable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    PlatformBackHandler(enabled, onBack)
}