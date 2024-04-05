package amaterek.util.ui.navigation.sample.ui

import amaterek.util.ui.navigation.sample.ui.navigation.ChooseNavigatorScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChooseNavigatorScreen()
        }
    }
}
