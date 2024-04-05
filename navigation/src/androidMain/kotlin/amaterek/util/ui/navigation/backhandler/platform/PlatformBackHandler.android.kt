package amaterek.util.ui.navigation.backhandler.platform

import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler as AndroidBackHandler

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = AndroidBackHandler(enabled, onBack)