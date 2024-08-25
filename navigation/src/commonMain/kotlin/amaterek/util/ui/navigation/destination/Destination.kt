package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.reflect.KClass

@Stable
interface Destination

typealias GraphDestination = KClass<out ScreenDestination>

@Immutable
data object PreviousDestination : Destination
