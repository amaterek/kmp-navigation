package amaterek.util.ui.navigation.jetpack

import amaterek.util.ui.navigation.NavigationGraph
import amaterek.util.ui.navigation.annotation.InternalNavigation

@OptIn(InternalNavigation::class)
internal val NavigationGraph.Entry.route: String
    get() = "$name?$ArgumentsName={$ArgumentsName}"

/**
 * Percent-encodes the value to be used as route query argument. Navigation decodes it back.
 */
internal fun String.encodeRouteArgument(): String = buildString {
    encodeToByteArray().forEach { byte ->
        val char = (byte.toInt() and BYTE_MASK).toChar()
        if (char.isUnreserved()) {
            append(char)
        } else {
            append('%')
            append(HEX_DIGITS[byte.toInt() shr HALF_BYTE_BITS and HALF_BYTE_MASK])
            append(HEX_DIGITS[byte.toInt() and HALF_BYTE_MASK])
        }
    }
}

private fun Char.isUnreserved(): Boolean =
    this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this in UNRESERVED_SYMBOLS

internal const val ArgumentsName = "destination"

private const val UNRESERVED_SYMBOLS = "-._~"
private const val HEX_DIGITS = "0123456789ABCDEF"
private const val BYTE_MASK = 0xFF
private const val HALF_BYTE_MASK = 0x0F
private const val HALF_BYTE_BITS = 4
