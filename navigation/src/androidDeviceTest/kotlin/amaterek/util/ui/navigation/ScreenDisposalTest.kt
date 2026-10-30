package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.common.ObjectDestination1
import amaterek.util.ui.navigation.common.ObjectDestination2
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.popUpTo
import amaterek.util.ui.navigation.destination.popUpToFirst
import amaterek.util.ui.navigation.jetpack.JetpackNavigationHost
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.nav3.Nav3NavigationHost
import amaterek.util.ui.navigation.nav3.rememberNav3Navigator
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The view models of the screens show that a removed screen is disposed. Voyager has screen view models only on
// Android, so the tests run only there.
class JetpackScreenDisposalTest : ScreenDisposalTest(
    rememberNavigator = { startDestination, graph ->
        rememberJetpackNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { JetpackNavigationHost(it) },
)

class Nav3ScreenDisposalTest : ScreenDisposalTest(
    rememberNavigator = { startDestination, graph ->
        rememberNav3Navigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { Nav3NavigationHost(it) },
)

class VoyagerScreenDisposalTest : ScreenDisposalTest(
    rememberNavigator = { startDestination, graph ->
        rememberVoyagerNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { VoyagerNavigationHost(it) },
)

@OptIn(ExperimentalTestApi::class)
abstract class ScreenDisposalTest(
    private val rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: NavigationGraph,
    ) -> Navigator,
    private val navigationHost: @Composable (Navigator) -> Unit,
) {

    private lateinit var navigator: Navigator

    @BeforeTest
    fun setup() {
        clearedViewModels.clear()
    }

    @Test
    fun disposesScreenPoppedByBack() = test {
        navigate { navigateTo(ViewModelDestinationA) }

        navigate { navigateBack() }

        assertEquals(listOf("A"), clearedViewModels)
    }

    @Test
    fun disposesScreensPoppedUpToFirstWithReplacement() = test {
        navigate { navigateTo(ViewModelDestinationA) }
        navigate { navigateTo(ViewModelDestinationB) }

        navigate { navigateTo(ViewModelDestinationB.popUpToFirst()) }

        assertEquals(listOf("A", "B"), clearedViewModels.sorted())
    }

    @Test
    fun disposesScreensPoppedUpToMiddleWithReplacement() = test {
        navigate { navigateTo(ViewModelDestinationA) }
        navigate { navigateTo(ViewModelDestinationB) }
        navigate { navigateTo(ObjectDestination2) }

        navigate { navigateTo(ObjectDestination2.popUpTo(ViewModelDestinationA)) }

        assertEquals(listOf("B"), clearedViewModels)
    }

    @Test
    fun disposesDialogPoppedByScreen() = test {
        navigate { navigateTo(ViewModelDialogDestination) }

        navigate { navigateTo(ViewModelDestinationA) }

        assertEquals(listOf("Dialog"), clearedViewModels)
    }

    private fun test(block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent {
            navigator = rememberNavigator(
                ObjectDestination1,
                navigationGraph {
                    screen<ObjectDestination1>()
                    screen<ObjectDestination2>()
                    screen<ViewModelDestinationA>()
                    screen<ViewModelDestinationB>()
                    dialog<ViewModelDialogDestination>()
                },
            )
            navigationHost(navigator)
        }
        block()
    }

    private fun ComposeUiTest.navigate(action: Navigator.() -> Unit) {
        runOnUiThread { navigator.action() }
        waitForIdle()
    }
}

private val clearedViewModels = mutableListOf<String>()

private class TestViewModel(private val name: String) : ViewModel() {

    override fun onCleared() {
        clearedViewModels += name
    }
}

@Composable
private fun ViewModelContent(name: String) {
    viewModel { TestViewModel(name) }
    Text(text = name)
}

@Serializable
private data object ViewModelDestinationA : ScreenDestination {

    @Composable
    override fun Content() = ViewModelContent("A")
}

@Serializable
private data object ViewModelDestinationB : ScreenDestination {

    @Composable
    override fun Content() = ViewModelContent("B")
}

@Serializable
private data object ViewModelDialogDestination : DialogDestination {

    @Composable
    override fun Content() = ViewModelContent("Dialog")
}
