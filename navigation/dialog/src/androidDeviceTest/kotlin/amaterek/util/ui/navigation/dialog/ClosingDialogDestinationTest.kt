package amaterek.util.ui.navigation.dialog

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.navigateBackWithResult
import amaterek.util.ui.navigation.navigationGraph
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class JetpackClosingDialogDestinationTest : ClosingDialogDestinationTest(JetpackTestNavigationHost)

@RunWith(AndroidJUnit4::class)
class VoyagerClosingDialogDestinationTest : ClosingDialogDestinationTest(VoyagerTestNavigationHost)

@Suppress("TooManyFunctions")
abstract class ClosingDialogDestinationTest(private val host: TestNavigationHost) {

    @get:Rule
    val composeTestRule = createComposeRule()

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

        composeTestRule.runOnUiThread { dialogScope.onClosed() }

        expectOpenDialog(isClosing = false)
    }

    @Test
    fun delegatesOtherNavigation() = test {
        openDialog(ResultDialogDestination)

        composeTestRule.runOnUiThread { dialogNavigator.navigateTo(TestScreenDestination2) }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(ScreenNameTag).assertTextEquals(TestScreenDestination2.name)
        assertEquals(navigator.backStack.size - 1, navigator.backStack.lastIndexOf(TestScreenDestination2))
    }

    private fun test(block: () -> Unit) {
        composeTestRule.setContent {
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
        composeTestRule.runOnUiThread { navigator.navigateTo(destination) }
        expectOpenDialog(isClosing = false)
    }

    private fun startClosing(action: () -> Unit) {
        composeTestRule.runOnUiThread(action)
        expectOpenDialog(isClosing = true)
    }

    private fun expectOpenDialog(isClosing: Boolean) = with(composeTestRule) {
        waitForIdle()
        onNodeWithTag(DialogTag)
            .assertIsDisplayed()
            .assertTextEquals(isClosing.toString())
        onNodeWithTag(ScreenResultTag).assertTextEquals("")
    }

    private fun expectClosedDialog(result: TestDialogResult?) = with(composeTestRule) {
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

@Serializable
private data object ResultDialogDestination : ClosingDialogDestination<TestDialogResult>() {

    @Composable
    override fun ClosingDialogScope<TestDialogResult>.DialogContent() {
        SideEffect { resultDialogScope = this }
        TestDialogContent()
    }

    override fun dismissResult() = TestDialogResult("dismissed")
}

@Serializable
private data object NoResultDialogDestination : ClosingDialogDestination<Nothing>() {

    @Composable
    override fun ClosingDialogScope<Nothing>.DialogContent() {
        TestDialogContent()
    }
}

@Composable
private fun ClosingDialogScope<*>.TestDialogContent() {
    val navigator = LocalNavigator.current
    SideEffect {
        dialogNavigator = navigator
        dialogScope = this
    }
    BasicText(
        modifier = Modifier.testTag(DialogTag),
        text = isClosing.toString(),
    )
}
