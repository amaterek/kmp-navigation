package amaterek.util.ui.navigation.sample.ui

import amaterek.util.ui.navigation.sample.ComposeApp
import amaterek.util.ui.navigation.sample.ui.navigation.PlatformNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    private lateinit var platformNavigation: PlatformNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        platformNavigation = PlatformNavigation(this)
        setContent {
            ComposeApp(platformNavigation)
        }
    }
}
