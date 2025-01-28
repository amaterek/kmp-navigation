package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.destination.ScreenDestination
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass

internal val ScreenDestination.baseRoute: String
    get() = this::class.definitionOfArgument.let {
        if (it.isNullOrEmpty()) routeWithoutArgument else "$routeWithoutArgument={$it}"
    }

internal val KClass<out ScreenDestination>.baseRoute: String
    get() = definitionOfArgument.let {
        if (it.isNullOrEmpty()) routeWithoutArgument else "$routeWithoutArgument={$it}"
    }

internal val ScreenDestination.route: String
    get() = argument.let {
        if (it.isNullOrEmpty()) routeWithoutArgument else "$routeWithoutArgument={$it}"
    }

internal inline val ScreenDestination.argument: String?
    get() {
        if (this::class.objectInstance != null) return null
        return serializeDestination()
    }

private inline val ScreenDestination.routeWithoutArgument: String
    get() = this::class.routeWithoutArgument

private inline val KClass<out ScreenDestination>.routeWithoutArgument: String
    get() = qualifiedName!!
        .replace('$', '-')

private inline val KClass<out ScreenDestination>.definitionOfArgument: String?
    get() {
        if (objectInstance != null) return null
        return ArgumentsName
    }

@OptIn(ExperimentalEncodingApi::class)
private fun ScreenDestination.serializeDestination(): String =
    ByteArrayOutputStream().use { outputStream ->
        ObjectOutputStream(outputStream).use {
            it.writeObject(this)
        }
        Base64.UrlSafe.encode(outputStream.toByteArray())
    }

@OptIn(ExperimentalEncodingApi::class)
internal fun String.deserializeDestination(): ScreenDestination =
    trim('{', '}').run {
        Base64.UrlSafe.decode(this).let { data ->
            ObjectInputStream(ByteArrayInputStream(data)).use {
                it.readObject()
            }
        } as ScreenDestination
    }

internal const val ArgumentsName = "destination"

@Suppress("UNCHECKED_CAST")
internal fun String.routeToScreenDestinationClass(): KClass<out ScreenDestination> =
    lastIndexOf("=")
        .let { if (it < 0) this else substring(0, it) }
        .run { Class.forName(replace('-', '$')).kotlin as KClass<out ScreenDestination> }