package amaterek.util.ui.navigation.dialog

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.popUpTo
import amaterek.util.ui.navigation.destination.replace
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.navigateBackWithResult
import amaterek.util.ui.navigation.navigationGraph
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class JetpackClosingDialogDestinationTest : ClosingDialogDestinationTest(JetpackTestNavigationHost)

class Nav3ClosingDialogDestinationTest : ClosingDialogDestinationTest(Nav3TestNavigationHost)

class VoyagerClosingDialogDestinationTest : ClosingDialogDestinationTest(VoyagerTestNavigationHost)

@OptIn(ExperimentalTestApi::class)
@Suppress("TooManyFunctions")
abstract class ClosingDialogDestinationTest(private val host: TestNavigationHost) {

    private lateinit var composeUiTest: ComposeUiTest

    private lateinit var navigator: Navigator

    @Test
    fun deliversResultAfterClosed() = test {
        openDialog(ResultDialogDestination)

        startClosing { dialogNavigator.navigateBackWithResult(TestDialogResult("result")) }

        expectClosedDialog(result = TestDialogResult("result"))
    }

    @Test
    fun deliversDismissResultOnBackNavigation() = test {
        openDialog(ResultDialogDestination)

        startClosing { dialogNavigator.navigateBack() }

        expectClosedDialog(result = ResultDialogDestination.dismissResult())
    }

    @Test
    fun deliversNoResultOnDismissWithoutDismissResult() = test {
        openDialog(NoResultDialogDestination)

        startClosing { dialogNavigator.navigateBack() }

        expectClosedDialog(result = null)
    }

    @Test
    fun ignoresCloseWhileClosing() = test {
        openDialog(ResultDialogDestination)

        startClosing {
            resultDialogScope.close(TestDialogResult("first"))
            resultDialogScope.close(TestDialogResult("second"))
            dialogNavigator.navigateBack()
        }

        expectClosedDialog(result = TestDialogResult("first"))
    }

    @Test
    fun ignoresClosedWhenNotClosing() = test {
        openDialog(ResultDialogDestination)

        composeUiTest.runOnUiThread { dialogScope.onClosed() }

        expectOpenDialog(isClosing = false)
    }

    @Test
    fun pausesScreenBelowDialog() = test {
        composeUiTest.onNodeWithTag(ScreenLifecycleTag).assertTextEquals(Lifecycle.State.RESUMED.name)

        openDialog(ResultDialogDestination)
        composeUiTest.onNodeWithTag(ScreenLifecycleTag).assertTextEquals(Lifecycle.State.STARTED.name)

        startClosing { dialogNavigator.navigateBack() }
        expectClosedDialog(result = ResultDialogDestination.dismissResult())
        composeUiTest.onNodeWithTag(ScreenLifecycleTag).assertTextEquals(Lifecycle.State.RESUMED.name)
    }

    @Test
    fun keepsDialogBelowAnotherDialogPaused() = test {
        openDialog(ResultDialogDestination)

        composeUiTest.runOnUiThread { navigator.navigateTo(NoResultDialogDestination) }
        composeUiTest.waitForIdle()
        expectLifecycle(ResultDialogLifecycleTag, Lifecycle.State.STARTED)
        expectLifecycle(NoResultDialogLifecycleTag, Lifecycle.State.RESUMED)
        expectLifecycle(ScreenLifecycleTag, Lifecycle.State.STARTED)

        composeUiTest.runOnUiThread { noResultDialogScope.dismiss() }
        composeUiTest.waitForIdle()
        composeUiTest.runOnUiThread { noResultDialogScope.onClosed() }
        composeUiTest.waitForIdle()
        composeUiTest.onNodeWithTag(NoResultDialogLifecycleTag).assertDoesNotExist()
        expectLifecycle(ResultDialogLifecycleTag, Lifecycle.State.RESUMED)
        expectLifecycle(ScreenLifecycleTag, Lifecycle.State.STARTED)
    }

    @Test
    fun popsDialogWhenScreenIsOpenedAboveIt() = test {
        openDialog(ResultDialogDestination)

        composeUiTest.runOnUiThread { dialogNavigator.navigateTo(TestScreenDestination2) }
        composeUiTest.waitForIdle()
        composeUiTest.onNodeWithTag(DialogTag).assertDoesNotExist()
        composeUiTest.onNodeWithTag(ScreenNameTag).assertTextEquals(TestScreenDestination2.name)
        assertEquals(-1, navigator.backStack.lastIndexOf(ResultDialogDestination))

        composeUiTest.runOnUiThread { navigator.navigateBack() }
        composeUiTest.waitForIdle()
        composeUiTest.onNodeWithTag(DialogTag).assertDoesNotExist()
        composeUiTest.onNodeWithTag(ScreenNameTag).assertTextEquals(TestScreenDestination1.name)
    }

    @Test
    fun popsDialogsWhenScreenReplacesDialogAboveThem() = test {
        openDialog(ResultDialogDestination)
        composeUiTest.runOnUiThread { navigator.navigateTo(NoResultDialogDestination) }
        composeUiTest.waitForIdle()

        composeUiTest.runOnUiThread { navigator.navigateTo(TestScreenDestination2.replace()) }
        composeUiTest.waitForIdle()

        expectOnlyScreenAboveStart(TestScreenDestination2)
    }

    @Test
    fun popsDialogsWhenScreenReplacesDialogsUpToOneOfThem() = test {
        openDialog(ResultDialogDestination)
        composeUiTest.runOnUiThread { navigator.navigateTo(NoResultDialogDestination) }
        composeUiTest.runOnUiThread { navigator.navigateTo(ResultDialogDestination) }
        composeUiTest.waitForIdle()

        composeUiTest.runOnUiThread {
            navigator.navigateTo(TestScreenDestination2.popUpTo(NoResultDialogDestination, inclusive = true))
        }
        composeUiTest.waitForIdle()

        expectOnlyScreenAboveStart(TestScreenDestination2)
    }

    @Test
    fun deliversResultOnceWhenDialogIsOpenedAboveReceiver() = test {
        openDialog(ResultDialogDestination)

        // The screen receives the result below the next dialog, so it isn't the top destination
        composeUiTest.runOnUiThread {
            navigator.navigateBackWithResult(TestDialogResult("result"))
            navigator.navigateTo(NoResultDialogDestination)
        }
        composeUiTest.waitForIdle()
        composeUiTest.onNodeWithTag(ScreenResultTag).assertTextEquals(TestDialogResult("result").toString())

        composeUiTest.runOnUiThread { navigator.navigateBack() }
        composeUiTest.runOnUiThread { navigator.navigateTo(TestScreenDestination2) }
        composeUiTest.waitForIdle()
        composeUiTest.runOnUiThread { navigator.navigateBack() }
        composeUiTest.waitForIdle()

        composeUiTest.onNodeWithTag(ScreenNameTag).assertTextEquals(TestScreenDestination1.name)
        composeUiTest.onNodeWithTag(ScreenResultTag).assertTextEquals("")
    }

    private fun expectOnlyScreenAboveStart(screen: TestScreenDestination) = with(composeUiTest) {
        onNodeWithTag(DialogTag).assertDoesNotExist()
        onNodeWithTag(ScreenNameTag).assertTextEquals(screen.name)
        assertEquals(2, navigator.backStack.size)

        runOnUiThread { navigator.navigateBack() }
        waitForIdle()
        onNodeWithTag(DialogTag).assertDoesNotExist()
        onNodeWithTag(ScreenNameTag).assertTextEquals(TestScreenDestination1.name)
    }

    private fun expectLifecycle(tag: String, state: Lifecycle.State) {
        composeUiTest.onNodeWithTag(tag).assertTextEquals(state.name)
    }

    private fun test(block: () -> Unit) = runComposeUiTest {
        composeUiTest = this
        setContent {
            navigator = host.rememberNavigator(
                TestScreenDestination1,
                navigationGraph {
                    screen<TestScreenDestination1>()
                    screen<TestScreenDestination2>()
                    dialog<ResultDialogDestination>()
                    dialog<NoResultDialogDestination>()
                },
            )
            host.navigationHost(navigator)
        }
        block()
    }

    private fun openDialog(destination: ScreenDestination) {
        composeUiTest.runOnUiThread { navigator.navigateTo(destination) }
        expectOpenDialog(isClosing = false)
    }

    private fun startClosing(action: () -> Unit) {
        composeUiTest.runOnUiThread(action)
        expectOpenDialog(isClosing = true)
    }

    private fun expectOpenDialog(isClosing: Boolean) = with(composeUiTest) {
        waitForIdle()
        onNodeWithTag(DialogTag)
            .assertIsDisplayed()
            .assertTextEquals(isClosing.toString())
        onNodeWithTag(ScreenResultTag).assertTextEquals("")
    }

    private fun expectClosedDialog(result: TestDialogResult?) = with(composeUiTest) {
        runOnUiThread { dialogScope.onClosed() }
        waitForIdle()
        onNodeWithTag(DialogTag).assertDoesNotExist()
        onNodeWithTag(ScreenNameTag).assertTextEquals(TestScreenDestination1.name)
        onNodeWithTag(ScreenResultTag).assertTextEquals(result?.toString().orEmpty())
        assertEquals(1, navigator.backStack.size)
    }
}

private lateinit var dialogNavigator: Navigator

private lateinit var dialogScope: ClosingDialogScope<*>

private lateinit var resultDialogScope: ClosingDialogScope<TestDialogResult>

private lateinit var noResultDialogScope: ClosingDialogScope<Nothing>

private const val ResultDialogLifecycleTag = "ResultDialogLifecycle"

private const val NoResultDialogLifecycleTag = "NoResultDialogLifecycle"

@Serializable
private data object ResultDialogDestination : ClosingDialogDestination<TestDialogResult>() {

    @Composable
    override fun ClosingDialogScope<TestDialogResult>.DialogContent() {
        SideEffect { resultDialogScope = this }
        TestDialogContent(ResultDialogLifecycleTag)
    }

    override fun dismissResult() = TestDialogResult("dismissed")
}

@Serializable
private data object NoResultDialogDestination : ClosingDialogDestination<Nothing>() {

    @Composable
    override fun ClosingDialogScope<Nothing>.DialogContent() {
        SideEffect { noResultDialogScope = this }
        TestDialogContent(NoResultDialogLifecycleTag)
    }
}

@Composable
private fun ClosingDialogScope<*>.TestDialogContent(lifecycleTag: String) {
    val navigator = LocalNavigator.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    SideEffect {
        dialogNavigator = navigator
        dialogScope = this
    }
    Column {
        BasicText(
            modifier = Modifier.testTag(DialogTag),
            text = isClosing.toString(),
        )
        BasicText(
            modifier = Modifier.testTag(lifecycleTag),
            text = lifecycleState.name,
        )
    }
}
