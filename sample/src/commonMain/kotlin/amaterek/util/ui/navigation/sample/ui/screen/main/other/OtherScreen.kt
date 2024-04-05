package amaterek.util.ui.navigation.sample.ui.screen.main.other

import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.backhandler.BackHandler
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun OtherScreen() {

    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    LifecycleLogger()

    BackHandler { navigator.navigateBack() }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentDestination.toString(),
            textAlign = TextAlign.Center,
        )
    }
}