package com.fliprss.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fliprss.app.data.LibraryController
import com.fliprss.app.data.LibraryState

@Composable
internal fun AccountDialog(state: LibraryState, library: LibraryController, onClose: () -> Unit) {
    var server by rememberSaveable { mutableStateOf(state.account?.endpoint ?: "https://") }
    var username by rememberSaveable { mutableStateOf(state.account?.username.orEmpty()) }
    // Password deliberately stays out of saved-instance state.
    var password by remember { mutableStateOf("") }
    var allowHttp by rememberSaveable { mutableStateOf(state.account?.allowHttp ?: false) }
    var submitted by remember { mutableStateOf(false) }
    LaunchedEffect(state.connecting, state.accountRevision) {
        if (submitted && !state.connecting && state.loginError == null && state.account != null && !state.sessionExpired) {
            password = ""; onClose()
        }
    }
    val account = state.account
    AlertDialog(
        onDismissRequest = { if (!state.connecting) onClose() },
        title = { Text(if (account != null && !state.sessionExpired) "FreshRSS account" else "Connect to FreshRSS") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (account != null && !state.sessionExpired) {
                    Text("Signed in as ${account.username}")
                    Text(account.endpoint, style = MaterialTheme.typography.bodySmall)
                    Text("Read and saved changes sync to this account. Local feeds remain available in the FlipRSS tab. Sign out to forget this account.")
                    TextButton(enabled = !state.connecting, onClick = { library.signOut() }) { Text("Sign out") }
                } else {
                    Text("Enable API access in FreshRSS, then set an API password in your profile. Use that API password here.")
                    OutlinedTextField(server, { server = it }, label = { Text("FreshRSS URL") }, singleLine = true,
                        enabled = !state.connecting, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri))
                    OutlinedTextField(username, { username = it }, label = { Text("Username") }, singleLine = true, enabled = !state.connecting)
                    OutlinedTextField(password, { password = it }, label = { Text("API password") }, singleLine = true,
                        enabled = !state.connecting, visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                    Row {
                        Checkbox(allowHttp, { allowHttp = it }, enabled = !state.connecting)
                        Text("Allow unencrypted HTTP for a trusted local server", Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(enabled = !state.connecting, onClick = { library.signOut(); password = "" }) { Text("Forget saved sign-in") }
                }
                state.loginError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.connecting) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            if (account == null || state.sessionExpired) {
                Button(enabled = !state.connecting && username.isNotBlank() && password.isNotEmpty(), onClick = {
                    submitted = true; library.connect(server, username, password, allowHttp)
                }) { Text("Sign in") }
            } else TextButton(enabled = !state.connecting, onClick = onClose) { Text("Done") }
        },
        dismissButton = { TextButton(enabled = !state.connecting, onClick = onClose) { Text("Close") } },
    )
}
