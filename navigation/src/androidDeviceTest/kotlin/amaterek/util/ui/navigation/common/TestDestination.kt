package amaterek.util.ui.navigation.common

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.resultFlow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

interface TestDestination : ScreenDestination {

    val name: String

    val result: String
        get() = "$name-result"

    @Composable
    override fun Content() {
        val navigationResult by LocalNavigator.current.resultFlow.collectAsState("")

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.testTag(DestinationNameTag),
                text = name,
            )
            Text(
                modifier = Modifier.testTag(DestinationResultTag),
                text = navigationResult.toString(),
            )
        }
    }
}