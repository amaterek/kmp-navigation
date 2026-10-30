package amaterek.util.ui.navigation.sample.ui.navigation.finishappdialog

import amaterek.util.ui.navigation.destination.DialogDestination
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.window.DialogProperties

@Immutable
internal class AppFinishDialogDestination(cancelable: AppFinishDialogIsCancelable) : DialogDestination {

    // Shown by the app outside of a navigation graph, so it keeps its own dialog properties
    val dialogProperties =
        when (cancelable) {
            AppFinishDialogIsCancelable.Yes -> true
            AppFinishDialogIsCancelable.No -> false
        }.let {
            DialogProperties(
                dismissOnBackPress = it,
                dismissOnClickOutside = it,
            )
        }

    @Composable
    override fun Content() {
        AppFinishDialog()
    }
}

enum class AppFinishDialogIsCancelable {
    Yes,
    No,
}
