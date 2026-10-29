package amaterek.util.ui.navigation.common

import amaterek.util.ui.navigation.serialization.SkipForSerialization

data object ObjectDestination2 : TestDestination {

    @SkipForSerialization
    override val name = "ObjectDestination2"
}