package amaterek.util.ui.navigation.backhandler.platform

import androidx.compose.runtime.Composable

@Composable
internal expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
