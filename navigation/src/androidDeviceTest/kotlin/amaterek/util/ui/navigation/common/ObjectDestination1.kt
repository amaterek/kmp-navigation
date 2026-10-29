package amaterek.util.ui.navigation.common

import amaterek.util.ui.navigation.serialization.SkipForSerialization

data object ObjectDestination1 : TestDestination {

    @SkipForSerialization
    override val name = "ObjectDestination1"
}