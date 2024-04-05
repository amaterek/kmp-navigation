@file:Suppress("MatchingDeclarationName")

package amaterek.util.ui.navigation.sample.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.ui.window.ApplicationScope
import java.awt.Desktop
import java.net.URI
import kotlin.system.exitProcess

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Stable
actual class PlatformNavigation actual constructor(private val context: Any) {

    actual fun finishApp() {
        when (context) {
            is ApplicationScope -> context.exitApplication()
            else -> exitProcess(0)
        }
    }

    actual fun openLink(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}