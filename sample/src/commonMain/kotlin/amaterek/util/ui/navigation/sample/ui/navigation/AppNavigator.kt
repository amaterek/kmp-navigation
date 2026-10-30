package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.ui.navigation.Navigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.Destination
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.destination.NavigatorDestination
import amaterek.util.ui.navigation.destination.PreviousDestination
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.getResultOrNull
import amaterek.util.ui.navigation.destination.isPreviousWithResult
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

    override fun navigateTo(destination: Destination) = when {
        destination == PreviousDestination || destination.isPreviousWithResult -> {
            when {
                appDialogState.value != null -> {
                    appDialogState.value = null
                }
                destination.isPreviousWithResult -> {
                    when (val result = destination.getResultOrNull<Any>()) {
                        is AppFinishDialogIsCancelable ->
                            appDialogState.value = AppFinishDialogDestination(result)
                        else -> platformNavigation.finishApp()
                    }
                }
                else -> platformNavigation.finishApp()
            }
        }
        destination is AppLinkDestination -> platformNavigation.openLink(destination.link)
        destination is NavigatorDestination.RedirectToParent -> {
            when (val targetDestination = destination.destination) {
                is AppLinkDestination -> platformNavigation.openLink(targetDestination.link)
                else -> error("Unsupported destination: $targetDestination")
            }
        }
        else -> error("Unsupported destination: $destination")
    }

    override fun getResultFlow(navigationResultHandler: Any): Flow<Any?> = notSupported()

    override fun setResult(result: Any?) = notSupported()
}

private fun notSupported(): Nothing = error("Not supported")