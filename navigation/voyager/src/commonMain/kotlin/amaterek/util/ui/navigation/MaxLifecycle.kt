package amaterek.util.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Provides [content] with the lifecycle of [parent] limited to [maxState], like the max lifecycle of a Jetpack
 * navigation back stack entry.
 */
@Composable
internal fun MaxLifecycle(
    maxState: Lifecycle.State,
    parent: LifecycleOwner = LocalLifecycleOwner.current,
    content: @Composable () -> Unit,
) {
    val lifecycleOwner = remember(parent) { MaxLifecycleOwner(parent) }
    SideEffect { lifecycleOwner.maxState = maxState }
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.attach()
        onDispose { lifecycleOwner.detach() }
    }
    CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner, content = content)
}

private class MaxLifecycleOwner(private val parent: LifecycleOwner) : LifecycleOwner {

    override val lifecycle = LifecycleRegistry(this)

    var maxState = Lifecycle.State.RESUMED
        set(value) {
            field = value
            update()
        }

    private val observer = LifecycleEventObserver { _, _ -> update() }

    fun attach() = parent.lifecycle.addObserver(observer)

    fun detach() {
        parent.lifecycle.removeObserver(observer)
        moveTo(Lifecycle.State.DESTROYED)
    }

    private fun update() = moveTo(minOf(parent.lifecycle.currentState, maxState))

    private fun moveTo(state: Lifecycle.State) {
        // A lifecycle that was never created can't be destroyed
        if (state == Lifecycle.State.DESTROYED && lifecycle.currentState == Lifecycle.State.INITIALIZED) return
        lifecycle.currentState = state
    }
}
