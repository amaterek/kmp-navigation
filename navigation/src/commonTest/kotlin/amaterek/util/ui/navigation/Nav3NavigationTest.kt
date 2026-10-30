package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.nav3.rememberNav3Navigator
import amaterek.util.ui.navigation.transition.NoneScreenTransition

class Nav3NavigationTest : CommonNavigationTest(
    rememberNavigator = { startDestination, graph, parent ->
        rememberNav3Navigator(startDestination, graph, parent, NoneScreenTransition)
    }
)
