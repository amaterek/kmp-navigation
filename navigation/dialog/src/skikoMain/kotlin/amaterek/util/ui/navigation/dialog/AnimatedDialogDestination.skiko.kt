package amaterek.util.ui.navigation.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties

// Skiko dialogs are drawn in the window, so it's enough to drop the platform width, insets, scrim and animation.
// Without useSoftwareKeyboardInset = false the dialog moves its content above the keyboard itself and reports no ime
// inset to it.
@Composable
internal actual fun ConfigureAnimatedDialogWindow() = Unit

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun animatedDialogProperties() = DialogProperties(
    dismissOnBackPress = false,
    dismissOnClickOutside = false,
    usePlatformDefaultWidth = false,
    usePlatformInsets = false,
    useSoftwareKeyboardInset = false,
    scrimColor = Color.Transparent,
    animateTransition = false,
)
