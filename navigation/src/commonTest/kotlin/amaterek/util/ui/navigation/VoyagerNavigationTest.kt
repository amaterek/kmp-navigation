package amaterek.util.ui.navigation

import amaterek.util.ui.navigation.transition.NoneScreenTransition

class VoyagerNavigationTest : CommonNavigationTest(
    rememberNavigator = { startDestination, graph, parent ->
        rememberVoyagerNavigator(startDestination, graph, parent, NoneScreenTransition)
    }
)
