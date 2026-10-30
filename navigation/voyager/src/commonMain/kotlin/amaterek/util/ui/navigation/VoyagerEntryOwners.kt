package amaterek.util.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.viewmodel.ViewModelStoreProvider

/**
 * The lifecycle and view model store of every back stack entry, like a Jetpack navigation back stack entry. Voyager
 * provides them only on Android and per composition, so a covered screen would be destroyed.
 */
internal class VoyagerEntryOwners(
    private val parentLifecycleOwner: LifecycleOwner,
    val viewModelStoreProvider: ViewModelStoreProvider,
) {

    private val lifecycleOwners = mutableMapOf<VoyagerBackStackEntry, EntryLifecycleOwner>()

    fun lifecycleOwnerOf(entry: VoyagerBackStackEntry): EntryLifecycleOwner =
        lifecycleOwners.getOrPut(entry) { EntryLifecycleOwner(parentLifecycleOwner) }

    fun dispose(entry: VoyagerBackStackEntry) {
        lifecycleOwners.remove(entry)?.destroy()
        // Cleared when the entry leaves the composition, e.g. after its exit animation
        viewModelStoreProvider.clearKey(entry.key)
    }

    fun disposeLifecycles() {
        lifecycleOwners.values.forEach { it.destroy() }
        lifecycleOwners.clear()
    }
}

/**
 * Follows the lifecycle of [parent] up to [maxState]: RESUMED for the top destination, STARTED below a dialog,
 * CREATED when it isn't displayed and DESTROYED when it's removed from the back stack.
 */
internal class EntryLifecycleOwner(private val parent: LifecycleOwner) : LifecycleOwner {

    override val lifecycle = LifecycleRegistry(this)

    var maxState = Lifecycle.State.CREATED
        set(value) {
            field = value
            update()
        }

    private val observer = LifecycleEventObserver { _, _ -> update() }

    init {
        parent.lifecycle.addObserver(observer)
    }

    fun destroy() {
        parent.lifecycle.removeObserver(observer)
        moveTo(Lifecycle.State.DESTROYED)
    }

    private fun update() = moveTo(minOf(parent.lifecycle.currentState, maxState))

    private fun moveTo(state: Lifecycle.State) {
        val currentState = lifecycle.currentState
        // A destroyed lifecycle can't move and one that was never created can't be destroyed
        if (currentState == Lifecycle.State.DESTROYED) return
        if (state == Lifecycle.State.DESTROYED && currentState == Lifecycle.State.INITIALIZED) return
        lifecycle.currentState = state
    }
}
