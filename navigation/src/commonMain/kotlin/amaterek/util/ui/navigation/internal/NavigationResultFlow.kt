package amaterek.util.ui.navigation.internal

import amaterek.util.ui.navigation.annotation.InternalNavigation
import kotlinx.coroutines.flow.Flow

@InternalNavigation
interface NavigationResultFlow {

    val resultFlow: Flow<Any?>
}