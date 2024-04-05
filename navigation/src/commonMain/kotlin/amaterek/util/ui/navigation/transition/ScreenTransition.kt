package amaterek.util.ui.navigation.transition

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

data class ScreenTransition(

    val name: String,

    val enter: EnterTransition,
    val exit: ExitTransition,

    val popEnter: EnterTransition,
    val popExit: ExitTransition,
)

interface ScreenTransitionProvider {

    val transition: ScreenTransition
        get() = NoneScreenTransition
}

private const val TransitionDurationMillis = 300

private val fadeAnimationSpec = tween<Float>(durationMillis = TransitionDurationMillis)
private val slideAnimationSpec = tween<IntOffset>(durationMillis = TransitionDurationMillis)

val NoneScreenTransition = ScreenTransition(
    name = "NoneScreenTransition",
    enter = EnterTransition.None,
    exit = ExitTransition.None,
    popEnter = EnterTransition.None,
    popExit = ExitTransition.None,
)

val FadeScreenTransition = ScreenTransition(
    name = "FadeScreenTransition",
    enter = fadeIn(animationSpec = fadeAnimationSpec),
    exit = fadeOut(animationSpec = fadeAnimationSpec),
    popEnter = fadeIn(animationSpec = fadeAnimationSpec),
    popExit = fadeOut(animationSpec = fadeAnimationSpec),
)

val SlideHorizontallyScreenTransition = ScreenTransition(
    name = "SlideHorizontallyScreenTransition",
    enter = slideInHorizontally(animationSpec = slideAnimationSpec, initialOffsetX = { fullSize -> fullSize }),
    exit = slideOutHorizontally(animationSpec = slideAnimationSpec, targetOffsetX = { fullSize -> -fullSize }),
    popEnter = slideInHorizontally(animationSpec = slideAnimationSpec, initialOffsetX = { fullSize -> -fullSize }),
    popExit = slideOutHorizontally(animationSpec = slideAnimationSpec, targetOffsetX = { fullSize -> fullSize }),
)

val SlideVerticallyScreenTransition = ScreenTransition(
    name = "SlideVerticallyScreenTransition",
    enter = slideInVertically(animationSpec = slideAnimationSpec, initialOffsetY = { fullSize -> fullSize }),
    exit = slideOutVertically(animationSpec = slideAnimationSpec, targetOffsetY = { fullSize -> -fullSize }),
    popEnter = slideInVertically(animationSpec = slideAnimationSpec, initialOffsetY = { fullSize -> -fullSize }),
    popExit = slideOutVertically(animationSpec = slideAnimationSpec, targetOffsetY = { fullSize -> fullSize }),
)