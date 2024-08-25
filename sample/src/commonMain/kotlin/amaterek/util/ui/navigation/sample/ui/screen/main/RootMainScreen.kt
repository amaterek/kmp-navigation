package amaterek.util.ui.navigation.sample.ui.screen.main

import amaterek.util.log.Log
import amaterek.util.ui.navigation.LocalDestination
import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.backhandler.BackHandler
import amaterek.util.ui.navigation.canNavigateBack
import amaterek.util.ui.navigation.currentDestinationFlow
import amaterek.util.ui.navigation.destination.popUpTo
import amaterek.util.ui.navigation.navigateBack
import amaterek.util.ui.navigation.popUpTo
import amaterek.util.ui.navigation.resultFlow
import amaterek.util.ui.navigation.sample.LocalNavigatorProvider
import amaterek.util.ui.navigation.sample.ui.navigation.LifecycleLogger
import amaterek.util.ui.navigation.sample.ui.screen.main.home.HomeDestination
import amaterek.util.ui.navigation.sample.ui.screen.main.home.forresultdialog.HomeForResultDialogDestination
import amaterek.util.ui.navigation.sample.ui.screen.main.home.multiple.HomeMultipleDestination
import amaterek.util.ui.navigation.sample.ui.screen.main.other.OtherDestination
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kmp_navigation.sample.generated.resources.Res
import kmp_navigation.sample.generated.resources.back_svgrepo_com
import kmp_navigation.sample.generated.resources.close_svgrepo_com
import kmp_navigation.sample.generated.resources.home_svgrepo_com
import kmp_navigation.sample.generated.resources.other_1_svgrepo_com
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
internal fun RootMainScreen() {
    val navigator = LocalNavigator.current
    val currentDestination = LocalDestination.current

    val mainNavigator = LocalNavigatorProvider.current.rememberNavigator(
        startDestination = HomeDestination,
        graph = setOf(
            HomeDestination::class,
            OtherDestination::class,
            HomeForResultDialogDestination::class,
            HomeMultipleDestination::class,
        ),
        parent = navigator,
    )

    val currentMainDestination by mainNavigator.currentDestinationFlow.collectAsState()

    val mainNavigatorCanNavigateBack by remember {
        derivedStateOf {
            @Suppress("UNUSED_EXPRESSION")
            currentMainDestination // forces refreshing this callback
            mainNavigator.canNavigateBack()
        }
    }

    LifecycleLogger()

    BackHandler { navigator.navigateBack() }

    val navigationResultFlow = navigator.resultFlow

    LaunchedEffect(Unit) {
        navigationResultFlow
            .onEach {
                Log.i("MainScreen", "redirect result from parent navigator to main: result=$it")
            }
            .launchIn(this)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(currentDestination.toString())
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier.clickable { mainNavigator.navigateBack() },
                        painter = if (mainNavigatorCanNavigateBack) {
                            painterResource(Res.drawable.back_svgrepo_com)
                        } else {
                            painterResource(Res.drawable.close_svgrepo_com)
                        },
                        contentDescription = null,
                    )
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    onClick = { mainNavigator.popUpTo(HomeDestination) },
                    icon = { Icon(painterResource(Res.drawable.home_svgrepo_com), null) },
                    label = { Text("Home") },
                    selected = currentMainDestination == HomeDestination,
                )
                NavigationBarItem(
                    onClick = { mainNavigator.navigateTo(OtherDestination.popUpTo(HomeDestination)) },
                    icon = { Icon(painterResource(Res.drawable.other_1_svgrepo_com), null) },
                    label = { Text("Other") },
                    selected = currentMainDestination == OtherDestination,
                )
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            LocalNavigatorProvider.current.invoke(
                navigator = mainNavigator,
            )
        }
    }
}