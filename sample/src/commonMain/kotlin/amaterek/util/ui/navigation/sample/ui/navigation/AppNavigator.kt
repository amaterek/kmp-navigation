package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.sample.ui.navigation.finishappdialog.AppFinishDialogDestination
import amaterek.util.ui.navigation.sample.ui.navigation.finishappdialog.AppFinishDialogIsCancelable
import amaterek.util.ui.navigation.sample.ui.navigation.link.AppLinkDestination
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KClass

@OptIn(InternalNavigation::class)
class AppNavigator(
    private val appDialogState: MutableState<DialogDestination?>,
    private val platformNavigation: PlatformNavigation,
) : Navigator {

    override val backStack: Navigator.BackStack = object : Navigator.BackStack {
        override val size: Int = 0
        override val currentDestinationFlow: StateFlow<ScreenDestination>
            get() = notSupported()

        override fun lastIndexOf(destination: ScreenDestination): Int = -1
        override fun lastIndexOf(destination: KClass<out ScreenDestination>): Int = -1
    }

    @InternalNavigation
    override fun getResultFlow(destination: ScreenDestination): Flow<Any?> = notSupported()

    @InternalNavigation
    override fun setResult(result: Any?) = notSupported()

    override fun navigateTo(destination: Destination) = when (destination) {
        is PreviousDestination, is NavigatorDestination.WithResult -> {
            when {
                appDialogState.value != null -> {
                    appDialogState.value = null
                }
                destination is NavigatorDestination.WithResult -> {
                    when (val result = destination.result) {
                        is AppFinishDialogIsCancelable ->
                            appDialogState.value = AppFinishDialogDestination(result)
                        else -> platformNavigation.finishApp()
                    }
                }
                else -> platformNavigation.finishApp()
            }
        }
        is AppLinkDestination -> platformNavigation.openLink(destination.link)
        is NavigatorDestination.RedirectToParent -> {
            when (val targetDestination = destination.destination) {
                is AppLinkDestination -> platformNavigation.openLink(targetDestination.link)
                else -> error("Unsupported destination: $targetDestination")
            }
        }
        else -> error("Unsupported destination: $destination")
    }
}

private fun notSupported(): Nothing = error("Not supported")