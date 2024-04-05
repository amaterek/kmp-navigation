package amaterek.util.ui.navigation.internal

import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.DialogDestination
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog

@InternalNavigation
@Composable
fun NavigationDialog(
    dialogDestination: DialogDestination,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit = { dialogDestination.Content() },
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dialogDestination.dialogProperties,
        content = content,
    )
}