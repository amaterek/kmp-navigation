package amaterek.util.ui.navigation.dialog

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

// The dialog window takes the attributes and size of the activity window, without the window animation, so the
// content is laid out like a screen and animated by Compose. Needs usePlatformDefaultWidth = true and
// decorFitsSystemWindows = false.
@Composable
internal actual fun ConfigureAnimatedDialogWindow() {
    val activity = LocalContext.current.activity ?: return
    val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window ?: return
    val activityWindow = activity.window
    val parentView = LocalView.current.parent as View
    SideEffect {
        val dialogAttributes = WindowManager.LayoutParams().apply {
            copyFrom(activityWindow.attributes)
            type = dialogWindow.attributes.type
            format = PixelFormat.TRANSPARENT
            windowAnimations = 0
        }
        @Suppress("DEPRECATION")
        dialogWindow.apply {
            attributes = dialogAttributes
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
            decorView.systemUiVisibility = activityWindow.decorView.systemUiVisibility
        }
        parentView.layoutParams = FrameLayout.LayoutParams(activityWindow.decorView.width, activityWindow.decorView.height)
    }
}

internal actual fun animatedDialogProperties() = DialogProperties(
    dismissOnBackPress = false,
    dismissOnClickOutside = false,
    usePlatformDefaultWidth = true,
    decorFitsSystemWindows = false,
)

private val Context.activity: Activity?
    get() = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.activity
        else -> null
    }
