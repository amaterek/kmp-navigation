package amaterek.util.ui.navigation.dialog

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.isPreviousWithResult
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.navigateBackWithResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Dialog destination which is closed in two steps, so its content can be animated out before the dialog is popped.
 *
 * - [ClosingDialogScope.close] starts closing: [ClosingDialogScope.isClosing] becomes `true` and the content starts
 *   its exit.
 * - [ClosingDialogScope.onClosed] must be called by the content when its exit finished. Then the dialog is popped
 *   and the result is delivered, so the receiver can't navigate above the closing dialog.
 * - [LocalNavigator] in [DialogContent] closes the dialog on back navigation: [PreviousDestination] as
 *   [ClosingDialogScope.dismiss], [PreviousDestination] with result with that result. Other navigations are delegated.
 * - Back press and click outside should be handled by the content (disabled in the dialog properties) with
 *   [ClosingDialogScope.dismiss], otherwise the dialog is popped without closing.
 *
 * Please check also [AnimatedDialogDestination], which implements the closing with animations.
 */
abstract class ClosingDialogDestination<RESULT : Any> : DialogDestination {

    @Composable
    final override fun Content() {
        val navigator = LocalNavigator.current
        // Not rememberSaveable: the state holds the result, which isn't saveable
        var closingState by remember { mutableStateOf<ClosingState?>(null) }
        val dialogScope = remember(navigator) {
            object : ClosingDialogScope<RESULT> {

                private var isClosed = false

                override val isClosing: Boolean
                    get() = closingState != null

                override fun close(result: RESULT) = startClosing(result)

                override fun dismiss() = startClosing(dismissResult())

                override fun onClosed() {
                    val state = closingState ?: return
                    if (isClosed) return
                    isClosed = true
                    state.result?.let { navigator.navigateBackWithResult(it) } ?: navigator.navigateBack()
                }

                private fun startClosing(result: RESULT?) {
                    if (closingState == null) closingState = ClosingState(result)
                }
            }
        }
        val closingNavigator = remember(navigator, dialogScope) { ClosingDialogNavigator(navigator, dialogScope) }
        CompositionLocalProvider(LocalNavigator provides closingNavigator) {
            dialogScope.DialogContent()
        }
    }

    @Composable
    abstract fun ClosingDialogScope<RESULT>.DialogContent()

    /**
     * Result delivered when the dialog is dismissed, e.g. back press or [PreviousDestination]; `null` delivers no result.
     * A dialog which never returns a result uses `Nothing` as [RESULT].
     */
    open fun dismissResult(): RESULT? = null
}

@Stable
interface ClosingDialogScope<RESULT : Any> {

    /**
     * `true` after [close], the content should start its exit and call [onClosed] when it's finished.
     */
    val isClosing: Boolean

    /**
     * Starts closing the dialog with [result]. Called again while closing is ignored.
     */
    fun close(result: RESULT)

    /**
     * Starts closing the dialog with [ClosingDialogDestination.dismissResult]. Called again while closing is ignored.
     */
    fun dismiss()

    /**
     * Pops the closing dialog and delivers its result. Ignored when the dialog isn't closing.
     */
    fun onClosed()
}

private data class ClosingState(val result: Any?)

private class ClosingDialogNavigator<RESULT : Any>(
    private val navigator: Navigator,
    private val dialogScope: ClosingDialogScope<RESULT>,
) : Navigator by navigator {

    @Suppress("UNCHECKED_CAST")
    override fun navigateTo(destination: Destination) = when {
        destination == PreviousDestination -> dialogScope.dismiss()
        destination.isPreviousWithResult -> dialogScope.close((destination as NavigatorDestination.WithResult).result as RESULT)
        else -> navigator.navigateTo(destination)
    }
}
