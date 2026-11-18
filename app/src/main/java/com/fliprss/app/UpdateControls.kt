package com.fliprss.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fliprss.app.data.RELEASES_PAGE

@Composable internal fun UpdateControls(updater: AppUpdater = viewModel()) {
    val state by updater.state.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("App updates", style = MaterialTheme.typography.titleSmall)
        Text("FlipRSS ${updater.installedVersion}", style = MaterialTheme.typography.bodySmall)
        TextButton(enabled = !state.busy, onClick = updater::check) { Text("Check for updates") }
        state.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (state.update != null && state.file == null) Button(enabled = !state.busy, onClick = updater::download) { Text("Download update") }
        if (state.file != null) Button(enabled = !state.busy, onClick = updater::install) { Text("Install update") }
        TextButton(onClick = { runCatching { uri.openUri(RELEASES_PAGE) } }) { Text("Open GitHub Releases") }
    }
}
