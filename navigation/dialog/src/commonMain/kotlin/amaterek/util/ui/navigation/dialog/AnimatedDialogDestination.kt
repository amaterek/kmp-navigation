package amaterek.util.ui.navigation.dialog

import amaterek.util.ui.navigation.NavigationGraphBuilder
import amaterek.util.ui.navigation.transition.FadeScreenTransition
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.SlideVerticallyScreenTransition
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.window.DialogProperties
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.first

/**
 * [ClosingDialogDestination] whose content is animated over a scrim. Add it to the graph with [animatedDialog].
 *
 * The content enters with [transition]'s `enter` and exits with its `popExit`, so screen transitions can be used,
 * e.g. [SlideVerticallyScreenTransition] with [Alignment.BottomCenter] for a bottom sheet.
 * Likewise the scrim with [scrimTransition].
 *
 * In a `data class` override the properties with getters (`override val transition get() = ...`), so they aren't
 * serialized.
 */
abstract class AnimatedDialogDestination<RESULT : Any> : ClosingDialogDestination<RESULT>() {

    override val transition: ScreenTransition
        get() = FadeScreenTransition

    open val contentAlignment: Alignment
        get() = Alignment.Center

    open val scrimColor: Color
        get() = DefaultScrimColor

    open val scrimTransition: ScreenTransition
        get() = FadeScreenTransition

    /**
     * Whether back dismisses the dialog. When `false`, back is ignored while the dialog is displayed.
     */
    open val dismissOnBackPress: Boolean
        get() = true

    /**
     * Whether a click on the scrim dismisses the dialog.
     */
    open val dismissOnClickOutside: Boolean
        get() = true

    @Composable
    final override fun ClosingDialogScope<RESULT>.DialogContent() {
        ConfigureAnimatedDialogWindow()
        val contentTransition = transition
        val visibleState = rememberVisibleState()
        // The enter starts when the dialog is placed: on iOS the dialog measures its content a frame after it's
        // composed, and a transition started before that finishes at once without the enter animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onPlaced { if (!isClosing) visibleState.targetState = true },
        )
        AnimatedVisibility(
            visibleState = visibleState,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
            label = "AnimatedDialog",
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = contentAlignment,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateEnterExit(
                            enter = scrimTransition.enter,
                            exit = scrimTransition.popExit,
                            label = "AnimatedDialogScrim",
                        )
                        .background(scrimColor)
                        .clickable(
                            enabled = dismissOnClickOutside,
                            interactionSource = null,
                            indication = null,
                            onClick = ::dismiss,
                        ),
                )
                Box(
                    modifier = Modifier.animateEnterExit(
                        enter = contentTransition.enter,
                        exit = contentTransition.popExit,
                        label = "AnimatedDialogContent",
                    ),
                ) {
                    NavigationBackHandler(
                        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
                        onBackCompleted = { if (dismissOnBackPress) dismiss() },
                    )
                    AnimatedDialogContent()
                }
            }
        }
    }

    @Composable
    abstract fun ClosingDialogScope<RESULT>.AnimatedDialogContent()

    companion object {

        val DefaultScrimColor = Color.Black.copy(alpha = 0.32f)

        /**
         * Properties of a full screen dialog without the platform's animation, scrim, back and click outside
         * handling, which [AnimatedDialogDestination] replaces. Used by [animatedDialog].
         */
        val dialogProperties: DialogProperties = animatedDialogProperties()
    }
}

/**
 * Adds the animated dialog destination to the graph with [AnimatedDialogDestination.dialogProperties].
 */
inline fun <reified T : AnimatedDialogDestination<*>> NavigationGraphBuilder.animatedDialog() =
    dialog<T>(AnimatedDialogDestination.dialogProperties)

@Composable
private fun ClosingDialogScope<*>.rememberVisibleState(): MutableTransitionState<Boolean> {
    // Saved, so the content doesn't enter again when it's recreated, e.g. on Android configuration change
    var wasVisible by rememberSaveable { mutableStateOf(false) }
    val visibleState = remember { MutableTransitionState(initialState = wasVisible) }
    LaunchedEffect(isClosing) {
        wasVisible = true
        if (isClosing) {
            visibleState.targetState = false
            snapshotFlow { visibleState.isIdle && !visibleState.currentState }.first { it }
            onClosed()
        }
    }
    return visibleState
}

@Composable
internal expect fun ConfigureAnimatedDialogWindow()

internal expect fun animatedDialogProperties(): DialogProperties
