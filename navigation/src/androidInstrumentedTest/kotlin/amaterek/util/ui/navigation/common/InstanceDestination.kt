package amaterek.util.ui.navigation.common

import amaterek.util.ui.navigation.transition.ScreenTransitionProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

data class InstanceDestination(val argument: String) : TestDestination {

    override val name: String
        get() = "InstanceDestination-$argument"

    @Composable
    override fun Content() {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.testTag(DestinationNameTag),
                text = name,
            )
        }
    }

    companion object : ScreenTransitionProvider
}