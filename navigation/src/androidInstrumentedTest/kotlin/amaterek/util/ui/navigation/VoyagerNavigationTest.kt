package amaterek.util.ui.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoyagerNavigationTest : CommonNavigationTest(
    rememberNavigator = { startDestination, graph, parent ->
        rememberVoyagerNavigator(startDestination, graph, parent)
    }
)