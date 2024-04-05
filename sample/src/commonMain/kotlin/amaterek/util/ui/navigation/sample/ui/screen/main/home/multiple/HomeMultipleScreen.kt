package amaterek.util.ui.navigation.sample.ui.screen.main.home.multiple

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.backhandler.BackHandler
import amaterek.util.ui.navigation.destination.PopUpToDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.withResult
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.popUpToWithResult
import amaterek.util.ui.navigation.resultFlow
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import amaterek.util.ui.navigation.sample.ui.screen.main.home.HomeDestination
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Suppress("LongMethod")
@Composable
internal fun HomeMultipleScreen(level: Int) {

    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    val navigationResult by navigator.resultFlow.collectAsState(null)

    LifecycleLogger()

    BackHandler {
        navigator.navigateBack()
    }

    fun getResult() = "Result from level $level"

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "$currentDestination",
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Navigation result: $navigationResult",
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = {
                navigator.navigateTo(
                    HomeMultipleDestination(level + 1),
                )
            },
        ) {
            Text("Next")
        }
        Button(
            onClick = {
                navigator.navigateTo(PreviousDestination.withResult(getResult()))
            },
        ) {
            Text("Back")
        }
        Button(
            onClick = {
                navigator.navigateTo(PopUpToDestination(HomeMultipleDestination(level - 1)).withResult(getResult()))
            },
            enabled = level > 0,
        ) {
            Text("Pop up to last")
        }
        Button(
            onClick = {
                navigator.popUpToWithResult(HomeMultipleDestination::class, getResult(), inclusive = true)
            },
            enabled = level > 0,
        ) {
            Text("Pop up to last (by class, inclusive)")
        }
        Button(
            onClick = {
                if (level > 1) {
                    navigator.popUpToWithResult(HomeMultipleDestination(level - 1), getResult(), inclusive = true)
                }
            },
            enabled = level > 1,
        ) {
            Text("Pop up to last inclusive")
        }
        Button(
            onClick = {
                navigator.popUpToWithResult(HomeMultipleDestination(0), getResult())
            },
        ) {
            Text("Pop up to first")
        }
        Button(
            onClick = {
                navigator.popUpToWithResult(HomeMultipleDestination(0), getResult(), inclusive = true)
            },
        ) {
            Text("Pop up to first inclusive (Home)")
        }
        Button(
            onClick = {
                navigator.popUpToWithResult(HomeDestination::class, getResult())
            },
        ) {
            Text("Pop up to Home")
        }
    }
}