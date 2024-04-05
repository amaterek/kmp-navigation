@file:Suppress("MatchingDeclarationName")

package amaterek.util.ui.navigation.sample.ui.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import kotlin.system.exitProcess

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Stable
actual class PlatformNavigation actual constructor(private val context: Any) {

    actual fun finishApp() {
        when (context) {
            is Activity -> context.finish()
            else -> exitProcess(0)
        }
    }

    actual fun openLink(url: String) {
        when (context) {
            is Context -> {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"))
                    context.startActivity(browserIntent)
                } catch (_: ActivityNotFoundException) {
                }
            }
            else -> Unit
        }
    }
}