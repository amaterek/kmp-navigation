package amaterek.util.ui.navigation.sample.ui.screen.main.home

import amaterek.util.log.Log
import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.destination.redirectToParent
import amaterek.util.ui.navigation.destination.redirectToParentIfNotInGraph
import amaterek.util.ui.navigation.navigateBackWithResult
import amaterek.util.ui.navigation.resultFlow
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import amaterek.util.ui.navigation.sample.ui.navigation.finishappdialog.AppFinishDialogIsCancelable
import amaterek.util.ui.navigation.sample.ui.navigation.link.AppLinkDestination
import amaterek.util.ui.navigation.sample.ui.screen.listdetail.ItemListDestination
import amaterek.util.ui.navigation.sample.ui.screen.main.LocalRootNavigationResultFlow
import amaterek.util.ui.navigation.sample.ui.screen.main.home.bottomsheet.HomeBottomSheetDestination
import amaterek.util.ui.navigation.sample.ui.screen.main.home.forresultdialog.HomeForResultDialogDestination
import amaterek.util.ui.navigation.sample.ui.screen.main.home.multiple.HomeMultipleDestination
import amaterek.util.ui.navigation.sample.ui.screen.withargumentandforresult.RootWithArgumentAndForResultDestination
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

@Suppress("LongMethod")
@Composable
internal fun HomeScreen() {

    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    var forResultDialogDestinationResult by rememberSaveable { mutableStateOf<String?>(null) }
    var bottomSheetResult by rememberSaveable { mutableStateOf<String?>(null) }
    var withArgumentAndForResultDestinationResult by rememberSaveable { mutableStateOf<String?>(null) }
    var notHandledDestinationResult by rememberSaveable { mutableStateOf<Any?>(null) }
    val navigationResultFlow = merge(
        LocalRootNavigationResultFlow.current,
        navigator.resultFlow,
    )

    LifecycleLogger()

    LaunchedEffect(Unit) {
        navigationResultFlow
            .onEach {
                Log.d(LogTag, "received result: $it")
                when (it) {
                    is HomeForResultDialogDestination.Companion.ForResultDialogDestinationResult ->
                        forResultDialogDestinationResult = it.value

                    is HomeBottomSheetDestination.Result -> bottomSheetResult = it.value

                    is RootWithArgumentAndForResultDestination.WithArgumentAndForResultDestinationResult ->
                        withArgumentAndForResultDestinationResult = it.value

                    else -> notHandledDestinationResult = it
                }
            }
            .launchIn(this)
    }

    Column(
        // verticalScroll keeps the minimum height of fillMaxSize, so the weighted spacers still center the buttons
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentDestination.toString(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Not handled result: $notHandledDestinationResult",
            textAlign = TextAlign.Center,
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                navigator.navigateTo(
                    AppLinkDestination("https://www.wikipedia.org")
                        .redirectToParentIfNotInGraph()
                )
            },
        ) {
            Text("Open Wiki in browser\nto app navigator", textAlign = TextAlign.Center)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigateTo(HomeForResultDialogDestination(0)) },
        ) {
            Text("For result dialog: $forResultDialogDestinationResult\nto main navigator", textAlign = TextAlign.Center)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigateTo(HomeBottomSheetDestination) },
        ) {
            Text("Bottom sheet: $bottomSheetResult\nto main navigator", textAlign = TextAlign.Center)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigateTo(HomeMultipleDestination(0)) },
        ) {
            Text("Multiple the same destination\nto main navigator", textAlign = TextAlign.Center)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigateTo(RootWithArgumentAndForResultDestination(text = "Test text").redirectToParent()) },
        ) {
            Text("With argument and for result screen: $withArgumentAndForResultDestinationResult\nto root navigator", textAlign = TextAlign.Center)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigateTo(ItemListDestination.redirectToParent()) },
        ) {
            Text("List-detail (two panes with Navigation 3)\nto root navigator", textAlign = TextAlign.Center)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigateBackWithResult(AppFinishDialogIsCancelable.Yes) },
        ) {
            Text("Close (show cancelable dialog)\nto app navigator", textAlign = TextAlign.Center)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigateBackWithResult(AppFinishDialogIsCancelable.No) },
        ) {
            Text("Close (show non cancelable dialog)\nto app navigator", textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

private const val LogTag = "HomeScreen"