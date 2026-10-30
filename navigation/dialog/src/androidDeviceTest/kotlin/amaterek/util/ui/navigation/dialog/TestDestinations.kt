package amaterek.util.ui.navigation.dialog

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.NavigationGraph
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.VoyagerNavigationHost
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.rememberVoyagerNavigator
import amaterek.util.ui.navigation.resultFlow
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.serialization.Serializable

const val ScreenNameTag = "ScreenName"

const val ScreenResultTag = "ScreenResult"

const val DialogTag = "Dialog"

class TestNavigationHost(
    val rememberNavigator: @Composable (startDestination: ScreenDestination, graph: NavigationGraph) -> Navigator,
    val navigationHost: @Composable (Navigator) -> Unit,
)

val JetpackTestNavigationHost = TestNavigationHost(
    rememberNavigator = { startDestination, graph ->
        rememberJetpackNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { JetpackNavigationHost(it) },
)

val VoyagerTestNavigationHost = TestNavigationHost(
    rememberNavigator = { startDestination, graph ->
        rememberVoyagerNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { VoyagerNavigationHost(it) },
)

interface TestScreenDestination : ScreenDestination {

    val name: String

    @Composable
    override fun Content() {
        val navigationResult by LocalNavigator.current.resultFlow.collectAsState("")
        Column(modifier = Modifier.fillMaxSize()) {
            BasicText(modifier = Modifier.testTag(ScreenNameTag), text = name)
            BasicText(modifier = Modifier.testTag(ScreenResultTag), text = navigationResult.toString())
        }
    }
}

@Serializable
data object TestScreenDestination1 : TestScreenDestination {

    override val name = "TestScreenDestination1"
}

@Serializable
data object TestScreenDestination2 : TestScreenDestination {

    override val name = "TestScreenDestination2"
}

data class TestDialogResult(val value: String)
