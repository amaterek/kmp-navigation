package amaterek.util.ui.navigation.sample.ui.screen.main.home.forresultdialog

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.navigateBackWithResult
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeForResultDialog(level: Int) {
    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    LifecycleLogger()

    Card {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = currentDestination.toString(),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = {
                        navigator.navigateBackWithResult(
                            HomeForResultDialogDestination.Companion.ForResultDialogDestinationResult("OK"),
                        )
                    },
                ) {
                    Text("Ok")
                }
                Button(
                    onClick = {
                        navigator.navigateBackWithResult(
                            HomeForResultDialogDestination.Companion.ForResultDialogDestinationResult("Cancel"),
                        )
                    },
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        navigator.navigateTo(HomeForResultDialogDestination(level + 1))
                    },
                ) {
                    Text("Next")
                }
            }
        }
    }
}