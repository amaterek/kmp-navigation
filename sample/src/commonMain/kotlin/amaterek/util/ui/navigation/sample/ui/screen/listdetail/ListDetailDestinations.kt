@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package amaterek.util.ui.navigation.sample.ui.screen.listdetail

import amaterek.util.ui.navigation.LocalNavigator
import amaterek.util.ui.navigation.destination.ScreenDestination
import amaterek.util.ui.navigation.destination.popUpTo
import amaterek.util.ui.navigation.navigateBack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable

/**
 * With the Navigation 3 navigator it's shown side by side with [ItemDetailDestination] on wide windows (tablet,
 * landscape, desktop) by the list-detail scene strategy of the root navigation host. The other navigators show them
 * one after another.
 */
@Serializable
internal data object ItemListDestination : ScreenDestination {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Button(modifier = Modifier.padding(16.dp), onClick = { navigator.navigateBack() }) {
                Text(text = "Back")
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items((1..ITEM_COUNT).toList()) { item ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navigator.navigateTo(ItemDetailDestination(item).popUpTo(ItemListDestination)) }
                            .padding(16.dp),
                        text = "Item $item",
                    )
                }
            }
        }
    }
}

@Serializable
internal data class ItemDetailDestination(val item: Int) : ScreenDestination {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Item $item", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { navigator.navigateBack() }) {
                Text(text = "Back")
            }
        }
    }
}

internal val ItemListPane: Map<String, Any> = ListDetailSceneStrategy.listPane(
    detailPlaceholder = { DetailPlaceholder() },
)

internal val ItemDetailPane: Map<String, Any> = ListDetailSceneStrategy.detailPane()

@Composable
private fun DetailPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Select an item")
    }
}

private const val ITEM_COUNT = 20
