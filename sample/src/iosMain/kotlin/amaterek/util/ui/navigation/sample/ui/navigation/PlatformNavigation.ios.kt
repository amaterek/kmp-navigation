@file:Suppress("MatchingDeclarationName")

package amaterek.util.ui.navigation.sample.ui.navigation

import androidx.compose.runtime.Stable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.system.exitProcess

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Stable
actual class PlatformNavigation actual constructor(context: Any) {

    actual fun finishApp() {
        exitProcess(0)
    }

    actual fun openLink(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}
