package amaterek.util.ui.navigation.internal

import amaterek.util.ui.navigation.annotation.InternalNavigation

@InternalNavigation
interface NavigationResultEmitter {

    fun emitNavigationResult(result: Any?)
}