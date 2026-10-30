package amaterek.util.ui.navigation.sample

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.navigationGraph
import amaterek.util.ui.navigation.sample.ui.navigation.AppNavigator
import amaterek.util.ui.navigation.sample.ui.navigation.NavigatorProvider
import amaterek.util.ui.navigation.sample.ui.navigation.PlatformNavigation
import amaterek.util.ui.navigation.sample.ui.navigation.finishappdialog.AppFinishDialogDestination
import amaterek.util.ui.navigation.sample.ui.screen.listdetail.ItemDetailDestination
import amaterek.util.ui.navigation.sample.ui.screen.listdetail.ItemDetailPane
import amaterek.util.ui.navigation.sample.ui.screen.listdetail.ItemListDestination
import amaterek.util.ui.navigation.sample.ui.screen.listdetail.ItemListPane
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
import androidx.compose.ui.window.Dialog

@Composable
internal fun ComposeApp(platformNavigation: PlatformNavigation, navigatorProvider: NavigatorProvider) {
    val appDialogDestination = remember { mutableStateOf<AppFinishDialogDestination?>(null) }

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
                    graph = navigationGraph {
                        screen<RootSplashDestination>()
                        screen<RootMainDestination>()
                        screen<RootWithArgumentAndForResultDestination>()
                        screen<ItemListDestination>(metadata = ItemListPane)
                        screen<ItemDetailDestination>(metadata = ItemDetailPane)
                    },
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

@Composable
private fun AppDialogHost(
    onDismissRequest: () -> Unit,
    dialogDestinationState: MutableState<AppFinishDialogDestination?>,
) {
    dialogDestinationState.value?.let {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = it.dialogProperties,
            content = { it.Content() },
        )
    }
}

internal val LocalPlatformNavigation = staticCompositionLocalOf<PlatformNavigation> {
    error("PlatformNavigation han not been provided")
}

internal val LocalNavigatorProvider = staticCompositionLocalOf<NavigatorProvider> {
    error("GetNavigationHost han not been provided")
}
