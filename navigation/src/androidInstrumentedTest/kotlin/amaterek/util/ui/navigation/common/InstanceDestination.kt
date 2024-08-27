package amaterek.util.ui.navigation.common

data class InstanceDestination(val argument: String) : TestDestination {

    override val name: String
        get() = "InstanceDestination-$argument"
}