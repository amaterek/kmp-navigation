package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.destination.ScreenDestination
import kotlin.reflect.KClass

internal val ScreenDestination.route: String
    inline get() = this::class.baseRoute

internal val KClass<out ScreenDestination>.route: String
    inline get() = baseRoute

private inline val KClass<out ScreenDestination>.baseRoute: String
    get() = qualifiedName!!
        .replace('$', '-')

@Suppress("UNCHECKED_CAST")
internal fun String.routeToScreenDestinationClass(): KClass<out ScreenDestination> =
    Class.forName(replace('-', '$')).kotlin as KClass<out ScreenDestination>