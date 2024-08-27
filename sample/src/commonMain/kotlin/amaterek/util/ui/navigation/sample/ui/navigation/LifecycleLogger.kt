package amaterek.util.ui.navigation.sample.ui.navigation

import amaterek.util.log.Log
import amaterek.util.ui.navigation.LocalDestination
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@NonRestartableComposable
@Composable
fun LifecycleLogger() {
    val name = LocalDestination.current::class.simpleName
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> Log.i(LOG_TAG, "$name: ON_CREATE")
                Lifecycle.Event.ON_START -> Log.i(LOG_TAG, "$name: ON_START")
                Lifecycle.Event.ON_RESUME -> Log.i(LOG_TAG, "$name: ON_RESUME")
                Lifecycle.Event.ON_PAUSE -> Log.i(LOG_TAG, "$name: ON_PAUSE")
                Lifecycle.Event.ON_STOP -> Log.i(LOG_TAG, "$name: ON_STOP")
                Lifecycle.Event.ON_DESTROY -> Log.i(LOG_TAG, "$name: ON_DESTROY")
                Lifecycle.Event.ON_ANY -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

private const val LOG_TAG = "Lifecycle"