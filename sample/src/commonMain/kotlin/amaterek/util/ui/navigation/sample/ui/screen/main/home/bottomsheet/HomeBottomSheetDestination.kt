package amaterek.util.ui.navigation.sample.ui.screen.main.home.bottomsheet

import amaterek.util.ui.navigation.dialog.AnimatedDialogDestination
import amaterek.util.ui.navigation.dialog.ClosingDialogScope
import amaterek.util.ui.navigation.transition.ScreenTransition
import amaterek.util.ui.navigation.transition.SlideVerticallyScreenTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import kotlinx.serialization.Serializable

@Serializable
data object HomeBottomSheetDestination : AnimatedDialogDestination<HomeBottomSheetDestination.Result>() {

    override val contentAlignment: Alignment
        get() = Alignment.BottomCenter

    override val transition: ScreenTransition
        get() = SlideVerticallyScreenTransition

    @Composable
    override fun ClosingDialogScope<Result>.AnimatedDialogContent() {
        HomeBottomSheet()
    }

    override fun dismissResult() = Result("Dismissed")

    data class Result(val value: String)
}
