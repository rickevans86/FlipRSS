package com.fliprss.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** At 320dp: 24dp logo + six 48dp targets + 8dp padding. Scaffold keeps it outside the list. */
@Composable internal fun ReadingToolbar(
    sourcePage: Boolean, hideRead: Boolean, allStories: Boolean, filtersEnabled: Boolean,
    canMarkAllRead: Boolean, showAdd: Boolean, addEnabled: Boolean, refreshEnabled: Boolean,
    onHideRead: (Boolean) -> Unit, onAllStories: () -> Unit, onMarkAllRead: () -> Unit,
    onAddFeed: () -> Unit, onRefresh: () -> Unit, onSettings: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(Modifier.statusBarsPadding().testTag("reading-toolbar")) {
            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_launcher), "FlipRSS", Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)))
                Spacer(Modifier.weight(1f))
                if (showAdd) IconButton(modifier = Modifier.size(48.dp), onClick = onAddFeed, enabled = addEnabled) { Icon(Icons.Outlined.Add, "Add feed") }
                if (sourcePage) {
                    IconToggleButton(hideRead, onHideRead, modifier = Modifier.size(48.dp), enabled = filtersEnabled,
                        colors = IconButtonDefaults.iconToggleButtonColors(checkedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Icon(if (hideRead) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Hide read stories")
                    }
                    IconButton(onClick = onAllStories, enabled = filtersEnabled, modifier = Modifier.size(48.dp).semantics { selected = allStories },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = if (allStories) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)) {
                        Icon(Icons.Outlined.LibraryBooks, "All stories")
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = onMarkAllRead, enabled = canMarkAllRead) { Icon(Icons.Outlined.Check, "Mark all read") }
                }
                IconButton(modifier = Modifier.size(48.dp), onClick = onRefresh, enabled = refreshEnabled) { Icon(Icons.Outlined.Refresh, "Refresh feeds") }
                IconButton(modifier = Modifier.size(48.dp), onClick = onSettings) { Icon(Icons.Outlined.Settings, "App settings") }
            }
            HorizontalDivider()
        }
    }
}
