package amaterek.util.ui.navigation.dialog

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.navigationGraph
import android.view.KeyEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JetpackAnimatedDialogDestinationTest : AnimatedDialogDestinationTest(JetpackTestNavigationHost)

@RunWith(AndroidJUnit4::class)
class Nav3AnimatedDialogDestinationTest : AnimatedDialogDestinationTest(Nav3TestNavigationHost)

@RunWith(AndroidJUnit4::class)
class VoyagerAnimatedDialogDestinationTest : AnimatedDialogDestinationTest(VoyagerTestNavigationHost)

@Suppress("TooManyFunctions")
abstract class AnimatedDialogDestinationTest(private val host: TestNavigationHost) {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navigator: Navigator

    @Test
    fun deliversResultAfterExitAnimation() = test {
        openDialog(DismissibleDialogDestination)

        composeTestRule.onNodeWithTag(DialogTag).performClick()

        expectClosedDialog(result = TestDialogResult("clicked"))
    }

    @Test
    fun dismissesOnBack() = test {
        openDialog(DismissibleDialogDestination)

        pressBack()

        expectClosedDialog(result = DismissibleDialogDestination.dismissResult())
    }

    @Test
    fun dismissesOnClickOutside() = test {
        openDialog(DismissibleDialogDestination)

        clickOutside()

        expectClosedDialog(result = DismissibleDialogDestination.dismissResult())
    }

    @Test
    fun ignoresBackWhenNotDismissibleOnBack() = test {
        openDialog(NotDismissibleDialogDestination)

        pressBack()

        expectOpenDialog()
    }

    @Test
    fun ignoresClickOutsideWhenNotDismissibleOnClickOutside() = test {
        openDialog(NotDismissibleDialogDestination)

        clickOutside()

        expectOpenDialog()
    }

    private fun test(block: () -> Unit) {
        composeTestRule.setContent {
            navigator = host.rememberNavigator(
                TestScreenDestination1,
                navigationGraph {
                    screen<TestScreenDestination1>()
                    animatedDialog<DismissibleDialogDestination>()
                    animatedDialog<NotDismissibleDialogDestination>()
                },
            )
            host.navigationHost(navigator)
        }
        block()
    }

    private fun openDialog(destination: AnimatedDialogDestination<*>) {
        composeTestRule.runOnUiThread { navigator.navigateTo(destination) }
        expectOpenDialog()
    }

    private fun pressBack() {
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
    }

    private fun clickOutside() {
        composeTestRule.onNodeWithTag(DialogTag).performTouchInput { click(Offset(x = width / 2f, y = -height * 2f)) }
    }

    private fun expectOpenDialog() = with(composeTestRule) {
        waitForIdle()
        onNodeWithTag(DialogTag).assertIsDisplayed()
        onNodeWithTag(ScreenResultTag).assertTextEquals("")
    }

    private fun expectClosedDialog(result: TestDialogResult) = with(composeTestRule) {
        waitForIdle()
        onNodeWithTag(DialogTag).assertDoesNotExist()
        onNodeWithTag(ScreenNameTag).assertTextEquals(TestScreenDestination1.name)
        onNodeWithTag(ScreenResultTag).assertTextEquals(result.toString())
    }
}

@Serializable
private data object DismissibleDialogDestination : AnimatedDialogDestination<TestDialogResult>() {

    @Composable
    override fun ClosingDialogScope<TestDialogResult>.AnimatedDialogContent() {
        TestDialogContent(onClick = { close(TestDialogResult("clicked")) })
    }

    override fun dismissResult() = TestDialogResult("dismissed")
}

@Serializable
private data object NotDismissibleDialogDestination : AnimatedDialogDestination<TestDialogResult>() {

    override val contentAlignment: Alignment
        get() = Alignment.BottomCenter

    override val dismissOnBackPress: Boolean
        get() = false

    override val dismissOnClickOutside: Boolean
        get() = false

    @Composable
    override fun ClosingDialogScope<TestDialogResult>.AnimatedDialogContent() {
        TestDialogContent(onClick = { close(TestDialogResult("clicked")) })
    }
}

@Composable
private fun TestDialogContent(onClick: () -> Unit) {
    BasicText(
        modifier = Modifier
            .testTag(DialogTag)
            .clickable(onClick = onClick),
        text = "Dialog",
    )
}
