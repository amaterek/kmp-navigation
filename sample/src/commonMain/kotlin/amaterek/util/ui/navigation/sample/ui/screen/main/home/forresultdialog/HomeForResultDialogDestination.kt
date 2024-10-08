package amaterek.util.ui.navigation.sample.ui.screen.main.home.forresultdialog

import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.DialogPropertiesProvider
import amaterek.util.ui.navigation.serialization.Serializable
import amaterek.util.ui.navigation.serialization.Serialize
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Serialize
data class HomeForResultDialogDestination(val level: Int) : DialogDestination, DialogPropertiesProvider {

    override val dialogProperties: DialogProperties
        get() = Companion.dialogProperties

    @Composable
    override fun Content() {
        HomeForResultDialog(level)
    }

    companion object : DialogPropertiesProvider {

        override val dialogProperties: DialogProperties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )

        @Serialize
        data class ForResultDialogDestinationResult(val value: String) : Serializable
    }
}