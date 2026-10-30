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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class JetpackScreenOwnersTest : ScreenOwnersTest(
    rememberNavigator = { startDestination, graph ->
        rememberJetpackNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { JetpackNavigationHost(it) },
)

// Navigation 3 destroys the lifecycle of an entry it doesn't display and creates a new one when it's displayed again
class Nav3ScreenOwnersTest : ScreenOwnersTest(
    rememberNavigator = { startDestination, graph ->
        rememberNav3Navigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { Nav3NavigationHost(it) },
    isLifecycleOfHiddenScreenKept = false,
)

@Suppress("DEPRECATION")
class VoyagerScreenOwnersTest : ScreenOwnersTest(
    rememberNavigator = { startDestination, graph ->
        rememberVoyagerNavigator(startDestination, graph, parent = null, NoneScreenTransition)
    },
    navigationHost = { VoyagerNavigationHost(it) },
)

/**
 * Every destination has its own lifecycle and view model store, which live as long as it's in the back stack.
 */
@OptIn(ExperimentalTestApi::class)
@Suppress("TooManyFunctions")
abstract class ScreenOwnersTest(
    private val rememberNavigator: @Composable (
        startDestination: ScreenDestination,
        graph: NavigationGraph,
    ) -> Navigator,
    private val navigationHost: @Composable (Navigator) -> Unit,
    private val isLifecycleOfHiddenScreenKept: Boolean = true,
) {

    private lateinit var navigator: Navigator

    @BeforeTest
    fun setup() {
        createdViewModels.clear()
        clearedViewModels.clear()
        lifecycleOwners.clear()
    }

    @Test
    fun keepsViewModelOfScreenInBackStack() = test {
        navigate { navigateTo(ViewModelDestinationA) }
        navigate { navigateTo(ViewModelDestinationB) }

        navigate { navigateBack() }

        assertEquals(listOf("A", "B"), createdViewModels)
        assertEquals(listOf("B"), clearedViewModels)
    }

    @Test
    fun stopsScreenInBackStack() = test {
        navigate { navigateTo(ViewModelDestinationA) }
        val lifecycleOwner = lifecycleOwners.getValue("A")
        assertEquals(Lifecycle.State.RESUMED, lifecycleOwner.lifecycle.currentState)

        navigate { navigateTo(ViewModelDestinationB) }
        if (isLifecycleOfHiddenScreenKept) {
            assertEquals(Lifecycle.State.CREATED, lifecycleOwner.lifecycle.currentState)
        } else {
            assertEquals(Lifecycle.State.DESTROYED, lifecycleOwner.lifecycle.currentState)
        }

        navigate { navigateBack() }
        assertEquals(Lifecycle.State.RESUMED, lifecycleOwners.getValue("A").lifecycle.currentState)
        if (isLifecycleOfHiddenScreenKept) {
            assertSame(lifecycleOwner, lifecycleOwners.getValue("A"))
        }
    }

    @Test
    fun pausesScreenBelowDialog() = test {
        navigate { navigateTo(ViewModelDestinationA) }

        navigate { navigateTo(ViewModelDialogDestination) }
        assertEquals(Lifecycle.State.STARTED, lifecycleOwners.getValue("A").lifecycle.currentState)
        assertEquals(Lifecycle.State.RESUMED, lifecycleOwners.getValue("Dialog").lifecycle.currentState)

        navigate { navigateBack() }
        assertEquals(Lifecycle.State.RESUMED, lifecycleOwners.getValue("A").lifecycle.currentState)
    }

    @Test
    fun createsSeparateViewModelsForEqualDestinations() = test {
        navigate { navigateTo(ViewModelDestinationA) }
        navigate { navigateTo(ObjectDestination2) }
        navigate { navigateTo(ViewModelDestinationA) }

        navigate { navigateBack() }
        navigate { navigateBack() }

        assertEquals(listOf("A", "A"), createdViewModels)
        assertEquals(listOf("A"), clearedViewModels)
    }

    @Test
    fun providesSavedStateHandle() = test {
        navigate { navigateTo(SavedStateHandleDestination) }

        assertEquals(listOf("SavedStateHandle"), createdViewModels)
    }

    @Test
    fun destroysScreenPoppedByBack() = test {
        navigate { navigateTo(ViewModelDestinationA) }
        val lifecycleOwner = lifecycleOwners.getValue("A")

        navigate { navigateBack() }

        assertEquals(listOf("A"), clearedViewModels)
        assertEquals(Lifecycle.State.DESTROYED, lifecycleOwner.lifecycle.currentState)
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
                    screen<SavedStateHandleDestination>()
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

private val createdViewModels = mutableListOf<String>()

private val clearedViewModels = mutableListOf<String>()

private val lifecycleOwners = mutableMapOf<String, LifecycleOwner>()

private class TestViewModel(private val name: String) : ViewModel() {

    init {
        createdViewModels += name
    }

    override fun onCleared() {
        clearedViewModels += name
    }
}

private class SavedStateHandleViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    init {
        savedStateHandle["key"] = "value"
        createdViewModels += "SavedStateHandle"
    }
}

@Serializable
private data object SavedStateHandleDestination : ScreenDestination {

    @Composable
    override fun Content() {
        viewModel { SavedStateHandleViewModel(createSavedStateHandle()) }
    }
}

@Composable
private fun ViewModelContent(name: String) {
    viewModel { TestViewModel(name) }
    val lifecycleOwner = LocalLifecycleOwner.current
    SideEffect { lifecycleOwners[name] = lifecycleOwner }
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
