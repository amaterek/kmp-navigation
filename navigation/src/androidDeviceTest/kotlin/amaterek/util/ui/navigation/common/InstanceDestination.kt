package amaterek.util.ui.navigation.common

import kotlinx.serialization.Serializable

@Serializable
data class InstanceDestination(val argument: String) : TestDestination {

    override val name: String
        get() = "InstanceDestination-$argument"
}
