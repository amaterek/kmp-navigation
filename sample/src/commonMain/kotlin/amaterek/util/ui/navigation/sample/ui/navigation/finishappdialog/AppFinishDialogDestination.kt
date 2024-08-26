package amaterek.util.ui.navigation.sample.ui.navigation.finishappdialog

import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.DialogPropertiesProvider
import amaterek.util.ui.navigation.serialization.Serialize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.window.DialogProperties

@Immutable
@Serialize
internal class AppFinishDialogDestination(val cancelable: AppFinishDialogIsCancelable) : DialogDestination, DialogPropertiesProvider {

    override val dialogProperties =
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