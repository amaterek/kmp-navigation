package amaterek.util.ui.navigation.sample.ui

import amaterek.util.ui.navigation.sample.ui.navigation.ChooseNavigatorScreen
import amaterek.util.ui.navigation.sample.ui.navigation.PlatformNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val platformNavigation = PlatformNavigation(this)
        setContent {
            ChooseNavigatorScreen(platformNavigation)
        }
    }
}
