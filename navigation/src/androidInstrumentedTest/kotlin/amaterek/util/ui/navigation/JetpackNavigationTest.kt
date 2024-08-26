package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JetpackNavigationTest : CommonNavigationTest(
    rememberNavigator = { startDestination, graph, parent ->
        rememberJetpackNavigator(startDestination, graph, parent, NoneScreenTransition)
    }
) {

    @Test
    override fun displaysStartInstanceDestination() {
        // Start destination with argument is not supported by JetpackNavigator
    }
}