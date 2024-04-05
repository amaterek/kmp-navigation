package amaterek.util.ui.navigation.sample.ui.navigation

import androidx.compose.runtime.Stable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@Stable
expect class PlatformNavigation(context: Any) {

    fun finishApp()

    fun openLink(url: String)
}
