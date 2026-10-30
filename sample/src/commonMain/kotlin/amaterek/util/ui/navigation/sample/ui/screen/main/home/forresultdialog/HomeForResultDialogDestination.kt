package amaterek.util.ui.navigation.sample.ui.screen.main.home.forresultdialog

import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.serialization.Serializable
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

data class HomeForResultDialogDestination(val level: Int) : DialogDestination {

    @Composable
    override fun Content() {
        HomeForResultDialog(level)
    }

    companion object {

        val dialogProperties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )

        data class ForResultDialogDestinationResult(val value: String) : Serializable
    }
}
