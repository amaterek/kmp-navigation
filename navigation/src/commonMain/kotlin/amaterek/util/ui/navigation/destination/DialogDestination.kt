package amaterek.util.ui.navigation.destination

import androidx.compose.runtime.Stable

/**
 * The interface must be implemented by all dialog destination that are used by default navigator.
 * Add it to the graph with [amaterek.util.ui.navigation.NavigationGraphBuilder.dialog], which takes its dialog
 * properties.
 *
 * Please check also [ScreenDestination]
 */
@Stable
interface DialogDestination : ScreenDestination
