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
    onAccount: () -> Unit, onClose: () -> Unit, onSettings: (AppSettings) -> Unit = library::updateSettings, onSpeech: () -> Unit = {}) {
    val settings = state.settings
    AlertDialog(onDismissRequest = onClose, title = { Text("Settings") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppTheme.entries.forEach { theme ->
                    FilterChip(selected = settings.theme == theme, onClick = { onSettings(settings.copy(theme = theme)) },
                        label = { Text(when (theme) { AppTheme.SYSTEM -> "System"; AppTheme.LIGHT -> "Light"; AppTheme.DARK -> "Dark"; AppTheme.OLED -> "OLED" }) })
                }
            }
            Text("Reader text size: ${settings.readerTextSize}")
            Slider(value = settings.readerTextSize.toFloat(), onValueChange = { onSettings(settings.copy(readerTextSize = it.roundToInt())) },
                valueRange = 16f..26f, steps = 9, modifier = Modifier.semantics { contentDescription = "Reader text size" })
            Text("A quieter reading experience.", fontFamily = FontFamily.Serif,
                fontSize = settings.readerTextSize.sp, lineHeight = (settings.readerTextSize * 1.55f).sp)
            HorizontalDivider()
            SettingsToggle("Show feed search", "Browse and search popular RSS feeds from the Search tab.", settings.showFeedSearch) {
                onSettings(settings.copy(showFeedSearch = it))
            }
            SettingsToggle("Hide read stories", "Applies to FreshRSS and FlipRSS; Saved always includes read stories.", settings.hideRead) {
                onSettings(settings.copy(hideRead = it))
            }
            SettingsToggle("Hide categories with no unread articles", "Hide empty categories and feeds on source pages.", settings.hideEmptyCategories) {
                onSettings(settings.copy(hideEmptyCategories = it))
            }
            SettingsToggle("Confirm mark all read", "When off, both Mark all read buttons apply immediately to the current source/category, including unloaded stories.", settings.confirmMarkAllRead) {
                onSettings(settings.copy(confirmMarkAllRead = it))
            }
            SettingsToggle("Mark as read on scroll", "Mark cards read after they scroll past the top of a source page.", settings.markReadOnScroll) {
                onSettings(settings.copy(markReadOnScroll = it))
            }
            SettingsToggle("Automatic mark read", "Mark an article read when its reader page settles.", settings.autoMarkRead) {
                onSettings(settings.copy(autoMarkRead = it))
            }
            SettingsToggle("Load full articles", "Fetch public article text from the publisher when you open a story. Feed text remains available if loading fails.", settings.autoLoadFullArticles) {
                onSettings(settings.copy(autoLoadFullArticles = it))
            }
            SettingsToggle("Show article images", "Display cover photos in cards and the reader.", settings.showImages) {
                onSettings(settings.copy(showImages = it))
            }
            HorizontalDivider()
            Text(state.account?.let { "Connected as ${it.username}" } ?: if (state.local) "Local feeds on this device" else "Using demo stories")
            TextButton(onClick = onAccount) { Text(if (state.account == null) "Connect FreshRSS (optional)" else "FreshRSS account") }
            HorizontalDivider()
            TextButton(onClick = onSpeech) { Text("Speech and voices") }
            UpdateControls()
            TextButton(onClick = { onSettings(AppSettings(useLocalFeeds = settings.useLocalFeeds)) }) { Text("Reset defaults") }
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
