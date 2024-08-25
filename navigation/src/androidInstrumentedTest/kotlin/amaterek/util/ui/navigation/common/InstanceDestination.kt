package amaterek.util.ui.navigation.common

import amaterek.util.ui.navigation.transition.ScreenTransitionProvider

data class InstanceDestination(val argument: String) : TestDestination {

    override val name: String
        get() = "InstanceDestination-$argument"

    companion object : ScreenTransitionProvider
}