package amaterek.util.ui.navigation.sample

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.annotation.InternalNavigation
import amaterek.util.ui.navigation.destination.DialogDestination
import amaterek.util.ui.navigation.internal.NavigationDialog
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.sample.ui.navigation.AppNavigator
import amaterek.util.ui.navigation.sample.ui.navigation.NavigatorProvider
import amaterek.util.ui.navigation.sample.ui.navigation.PlatformNavigation
import amaterek.util.ui.navigation.sample.ui.screen.main.RootMainDestination
import amaterek.util.ui.navigation.sample.ui.screen.splash.RootSplashDestination
import amaterek.util.ui.navigation.sample.ui.screen.withargumentandforresult.RootWithArgumentAndForResultDestination
import amaterek.util.ui.navigation.sample.ui.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
internal fun ComposeApp(platformNavigation: PlatformNavigation, navigatorProvider: NavigatorProvider) {
    val appDialogDestination = remember { mutableStateOf<DialogDestination?>(null) }

    val appNavigator = remember {
        AppNavigator(appDialogDestination, platformNavigation)
    }

    CompositionLocalProvider(
        LocalNavigator provides appNavigator,
        LocalNavigatorProvider provides navigatorProvider,
        LocalPlatformNavigation provides platformNavigation,
    ) {
        AppTheme {
            navigatorProvider(
                navigator = navigatorProvider.rememberNavigator(
                    startDestination = RootSplashDestination,
                    graph = setOf(
                        RootSplashDestination::class,
                        RootMainDestination::class,
                        RootWithArgumentAndForResultDestination::class,
                    ),
                    parent = appNavigator,
                )
            )

            AppDialogHost(
                onDismissRequest = { appNavigator.navigateBack() },
                dialogDestinationState = appDialogDestination
            )
        }
    }
}

@OptIn(InternalNavigation::class)
@Composable
private fun AppDialogHost(
    onDismissRequest: () -> Unit,
    dialogDestinationState: MutableState<DialogDestination?>,
) {
    dialogDestinationState.value?.let {
        NavigationDialog(
            dialogDestination = it,
            onDismissRequest = onDismissRequest,
        )
    }
}

internal val LocalPlatformNavigation = staticCompositionLocalOf<PlatformNavigation> {
    error("PlatformNavigation han not been provided")
}

internal val LocalNavigatorProvider = staticCompositionLocalOf<NavigatorProvider> {
    error("GetNavigationHost han not been provided")
}
