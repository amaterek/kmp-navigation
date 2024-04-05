package amaterek.util.ui.navigation.sample.ui.navigation.finishappdialog

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.sample.LocalPlatformNavigation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun AppFinishDialog() {

    val navigator = LocalNavigator.current
    val platformNavigation = LocalPlatformNavigation.current

    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = "Do you really want to finish the app",
                textAlign = TextAlign.Center,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { platformNavigation.finishApp() },
                ) {
                    Text("Yes")
                }
                Button(
                    onClick = { navigator.navigateBack() },
                ) {
                    Text("No")
                }
            }
        }
    }
}