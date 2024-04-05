package amaterek.util.ui.navigation.sample.ui.screen.withargumentandforresult

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.backhandler.BackHandler
import amaterek.util.ui.navigation.navigateBackWithResult
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun RootWithArgumentAndForResultScreen(text: String) {

    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    LifecycleLogger()

    BackHandler {
        navigator.navigateBackWithResult(
            RootWithArgumentAndForResultDestination.WithArgumentAndForResultDestinationResult("Unknown"),
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "${currentDestination}\nArgument=$text",
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = {
                navigator.navigateBackWithResult(
                    RootWithArgumentAndForResultDestination.WithArgumentAndForResultDestinationResult("OK"),
                )
            },
        ) {
            Text("Ok")
        }
        Button(
            onClick = {
                navigator.navigateBackWithResult(
                    RootWithArgumentAndForResultDestination.WithArgumentAndForResultDestinationResult("Cancel"),
                )
            },
        ) {
            Text("Cancel")
        }
    }
}