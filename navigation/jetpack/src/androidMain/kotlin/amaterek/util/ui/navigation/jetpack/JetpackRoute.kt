package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.destination.ScreenDestination
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.reflect.KClass

internal val ScreenDestination.baseRoute: String
    get() = this::class.definitionOfArgument.let {
        if (it.isNullOrEmpty()) routeWithoutArgument else "$routeWithoutArgument-{$it}"
    }

internal val KClass<out ScreenDestination>.baseRoute: String
    get() = definitionOfArgument.let {
        if (it.isNullOrEmpty()) routeWithoutArgument else "$routeWithoutArgument-{$it}"
    }

internal val ScreenDestination.route: String
    get() = argument.let {
        if (it.isNullOrEmpty()) routeWithoutArgument else "$routeWithoutArgument-{$it}"
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
        .replace('.', '_')
        .replace('$', '_') // FIXME Use regex

private inline val KClass<out ScreenDestination>.definitionOfArgument: String?
    get() {
        if (objectInstance != null) return null
        return ArgumentsName
    }

private fun ScreenDestination.serializeDestination(): String =
    ByteArrayOutputStream().use { outputStream ->
        ObjectOutputStream(outputStream).use {
            it.writeObject(this)
        }
        Base64.encodeToString(outputStream.toByteArray(), Base64Flags)
    }

internal fun String.deserializeDestination(): ScreenDestination =
    Base64.decode(this, Base64Flags).let { data ->
        ObjectInputStream(ByteArrayInputStream(data)).use {
            it.readObject()
        }
    } as ScreenDestination

private const val Base64Flags = Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE

internal const val ArgumentsName = "destination"
