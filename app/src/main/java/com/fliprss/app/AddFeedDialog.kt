package com.fliprss.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fliprss.app.data.LibraryController
import com.fliprss.app.data.LibraryState

@Composable internal fun AddFeedDialog(state: LibraryState, library: LibraryController,
    onAccount: () -> Unit, onClose: () -> Unit, initialUrl: String = "") {
    // Feed URLs can contain private query tokens; do not put them in saved state.
    var url by remember { mutableStateOf(initialUrl) }
    val initialRevision = remember { state.feedAddedRevision }
    LaunchedEffect(state.feedAddedRevision) { if (state.feedAddedRevision > initialRevision) onClose() }
    AlertDialog(onDismissRequest = { if (!state.addingFeed) onClose() }, title = { Text("Add feed") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (state.local) "Add an RSS or Atom XML feed directly to this device. HTTP feeds are unencrypted. FreshRSS is optional." else "Subscribe on your FreshRSS server. The feed will appear under Topics → Your feeds after refresh.")
            OutlinedTextField(url, { url = it }, label = { Text("Feed URL") },
                placeholder = { Text("https://example.org/feed.xml") }, singleLine = true,
                enabled = !state.addingFeed, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
            state.feedError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.addingFeed) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state.sessionExpired) TextButton(onClick = onAccount) { Text("Sign in again") }
        }
    }, confirmButton = {
        Button(enabled = url.isNotBlank() && !state.addingFeed && !state.sessionExpired,
            onClick = { library.addFeed(url) }) { Text(if (state.local) "Add to app" else "Add to FreshRSS") }
    }, dismissButton = { TextButton(enabled = !state.addingFeed, onClick = onClose) { Text("Cancel") } })
}
