package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.common.InstanceDestination
import amaterek.util.ui.navigation.jetpack.rememberJetpackNavigator
import amaterek.util.ui.navigation.transition.NoneScreenTransition
import kotlin.test.Test

class JetpackNavigationTest : CommonNavigationTest(
    rememberNavigator = { startDestination, graph, parent ->
        rememberJetpackNavigator(startDestination, graph, parent, NoneScreenTransition)
    }
) {

    @Test
    fun navigateToDestinationWithSpecialCharacters() = test {
        expectStartDestination(InstanceDestination("start ?&=/%#+ żółć {\"json\"}"))

        val destination = InstanceDestination("next ?&=/%#+ ąę 🚀 {\"json\": [1, 2]}")
        expectDestination(destination) { navigateTo(it) }
        destination.expectLastIndexInBackStack(1)
    }
}
