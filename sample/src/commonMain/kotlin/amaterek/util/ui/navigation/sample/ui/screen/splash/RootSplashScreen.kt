package amaterek.util.ui.navigation.sample.ui.screen.splash

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.backhandler.BackHandler
import amaterek.util.ui.navigation.destination.replaceAll
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import amaterek.util.ui.navigation.sample.ui.screen.main.RootMainDestination
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun RootSplashScreen() {

    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    LifecycleLogger()

    BackHandler { navigator.navigateBack() }

    fun finisSplash() {
        navigator.navigateTo(RootMainDestination.replaceAll())
    }

    LaunchedEffect(Unit) {
        launch {
            delay(3.seconds)
            finisSplash()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { finisSplash() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = currentDestination.toString(),
            textAlign = TextAlign.Center,
        )
    }
}