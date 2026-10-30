package amaterek.util.ui.navigation.sample.ui.screen.main.home.bottomsheet

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.dialog.ClosingDialogScope
import amaterek.util.ui.navigation.navigateBackWithResult
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun ClosingDialogScope<HomeBottomSheetDestination.Result>.HomeBottomSheet() {
    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    LifecycleLogger()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            )
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = currentDestination.toString(),
            textAlign = TextAlign.Center,
        )
        // Navigating back closes the bottom sheet with its exit animation, like close()
        Button(onClick = { navigator.navigateBackWithResult(HomeBottomSheetDestination.Result("Option 1")) }) {
            Text("Option 1 (navigator)")
        }
        Button(onClick = { close(HomeBottomSheetDestination.Result("Option 2")) }) {
            Text("Option 2 (scope)")
        }
    }
}
