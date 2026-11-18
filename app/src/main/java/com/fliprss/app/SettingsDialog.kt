package com.fliprss.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliprss.app.data.*
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun SettingsDialog(state: LibraryState, library: LibraryController,
    onAccount: () -> Unit, onClose: () -> Unit) {
    val settings = state.settings
    AlertDialog(onDismissRequest = onClose, title = { Text("Settings") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppTheme.entries.forEach { theme ->
                    FilterChip(selected = settings.theme == theme, onClick = { library.updateSettings(settings.copy(theme = theme)) },
                        label = { Text(when (theme) { AppTheme.SYSTEM -> "System"; AppTheme.LIGHT -> "Light"; AppTheme.DARK -> "Dark"; AppTheme.OLED -> "OLED" }) })
                }
            }
            Text("Reader text size: ${settings.readerTextSize}")
            Slider(value = settings.readerTextSize.toFloat(), onValueChange = { library.updateSettings(settings.copy(readerTextSize = it.roundToInt())) },
                valueRange = 16f..26f, steps = 9, modifier = Modifier.semantics { contentDescription = "Reader text size" })
            Text("A quieter reading experience.", fontFamily = FontFamily.Serif,
                fontSize = settings.readerTextSize.sp, lineHeight = (settings.readerTextSize * 1.55f).sp)
            HorizontalDivider()
            SettingsToggle("Show feed search", "Browse and search popular RSS feeds from the Search tab.", settings.showFeedSearch) {
                library.updateSettings(settings.copy(showFeedSearch = it))
            }
            SettingsToggle("Hide read stories", "Applies to Today; Saved always includes read stories.", settings.hideRead) {
                library.updateSettings(settings.copy(hideRead = it))
            }
            SettingsToggle("Hide categories with no unread articles", "Hide empty categories and feeds in Today and Topics.", settings.hideEmptyCategories) {
                library.updateSettings(settings.copy(hideEmptyCategories = it))
            }
            SettingsToggle("Automatic mark read", "Mark an article read when its reader page settles.", settings.autoMarkRead) {
                library.updateSettings(settings.copy(autoMarkRead = it))
            }
            SettingsToggle("Show article images", "Display cover photos in cards and the reader.", settings.showImages) {
                library.updateSettings(settings.copy(showImages = it))
            }
            HorizontalDivider()
            Text(state.account?.let { "Connected as ${it.username}" } ?: if (state.local) "Local feeds on this device" else "Using demo stories")
            TextButton(onClick = onAccount) { Text(if (state.demo || state.local) "Connect FreshRSS (optional)" else "FreshRSS account") }
            if (!state.local) TextButton(onClick = { library.useLocalFeeds(); onClose() }) { Text("Use local feeds") }
            HorizontalDivider()
            UpdateControls()
            TextButton(onClick = { library.updateSettings(AppSettings(useLocalFeeds = settings.useLocalFeeds)) }) { Text("Reset defaults") }
        }
    }, confirmButton = { TextButton(onClick = onClose) { Text("Done") } })
}

@Composable private fun SettingsToggle(title: String, description: String, checked: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().toggleable(value = checked, role = Role.Switch, onValueChange = change).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title); Text(description, style = MaterialTheme.typography.bodySmall) }
        Switch(checked, onCheckedChange = null)
    }
}
