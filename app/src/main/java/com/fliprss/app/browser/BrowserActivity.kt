package com.fliprss.app.browser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fliprss.app.data.AndroidSettingsStore
import com.fliprss.app.data.AppTheme
import org.mozilla.geckoview.GeckoView

class BrowserActivity : ComponentActivity() {
    private var model: BrowserModel? = null
    private var webPrompts: BrowserPrompts? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        val url = intent.getStringExtra("url")?.let(BrowserUrls::web)
        val settingsOnly = intent.getBooleanExtra("settings", false)
        if (url == null && !settingsOnly) { finish(); return }
        val result = runCatching { ViewModelProvider(this)[BrowserModel::class.java] }
        model = result.getOrNull()
        model?.start(url, intent.getStringExtra("readerRequest"))
        setContent {
            val theme = AndroidSettingsStore(this).load().theme
            val dark = theme == AppTheme.DARK || theme == AppTheme.OLED || (theme == AppTheme.SYSTEM && isSystemInDarkTheme())
            val colors = if (theme == AppTheme.OLED) darkColorScheme(background = Color.Black, surface = Color.Black)
                else if (dark) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colors) {
                model?.let { Browser(it, settingsOnly, ::finish) { token ->
                    setResult(RESULT_OK, Intent().putExtra("readerResult", token)); finish()
                } } ?: Scaffold { padding ->
                    Column(Modifier.padding(padding).padding(24.dp)) {
                        Text("The built-in browser could not start.")
                        if (url != null) TextButton(onClick = { openExternal(this@BrowserActivity, url) }) { Text("Open in external browser") }
                        TextButton(onClick = ::finish) { Text("Return to FlipRSS") }
                    }
                }
            }
        }
    }
    override fun onResume() { super.onResume(); webPrompts = BrowserPrompts(this); model?.setPrompts(webPrompts); model?.active(true) }
    override fun onPause() { model?.active(false); model?.answerPermission(false); webPrompts?.close(); model?.setPrompts(null); webPrompts = null; super.onPause() }
    companion object {
        fun open(context: Context, url: String) {
            val address = BrowserUrls.web(url) ?: return
            context.startActivity(Intent(context, BrowserActivity::class.java).putExtra("url", address))
        }
        fun articleIntent(context: Context, url: String, token: String) =
            Intent(context, BrowserActivity::class.java).putExtra("url", BrowserUrls.web(url)).putExtra("readerRequest", token)
        fun settings(context: Context) { context.startActivity(Intent(context, BrowserActivity::class.java).putExtra("settings", true)) }
    }
}
internal fun openExternal(context: Context, url: String) {
    val address = BrowserUrls.web(url) ?: return
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, address.toUri())) }
        .onFailure { Toast.makeText(context, "No external browser is available.", Toast.LENGTH_SHORT).show() }
}

@Composable private fun Browser(model: BrowserModel, settingsOnly: Boolean, close: () -> Unit, returnText: (String) -> Unit) {
    val addon by model.engine.state.collectAsStateWithLifecycle()
    LaunchedEffect(model.returnArticle) { model.returnArticle?.let(returnText) }
    var settings by rememberSaveable { mutableStateOf(settingsOnly) }
    var licences by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val tab = model.current
    fun back() { when {
        tab?.back == true -> tab.session.goBack()
        model.tabs.size > 1 && tab != null -> model.closeTab(tab.session)
        else -> close()
    } }
    BackHandler { back() }
    Scaffold(topBar = {
        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Text(tab?.title.orEmpty(), Modifier.weight(1f), maxLines = 1, style = MaterialTheme.typography.labelSmall)
                TextButton(enabled = tab?.url?.let { it.startsWith("http") && BrowserUrls.web(it) != null } == true,
                    onClick = { tab?.url?.let { openExternal(context, it) } }) { Text("External") }
            }
            if (tab?.loading == true || addon.busy || addon.adblockBusy || model.capturing) LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).navigationBarsPadding()) {
            (model.error ?: addon.error)?.let { message ->
                Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
                if (!model.ready) TextButton(onClick = { model.configure(model.engine.requestedEnabled) }) { Text("Retry browser setup") }
            }
            if (tab != null) AndroidView(factory = { GeckoView(it) }, modifier = Modifier.fillMaxSize(),
                onRelease = { it.releaseSession() }, update = { view ->
                    if (view.session != tab.session) { view.releaseSession(); view.setSession(tab.session) }
                })
        }
    }
    val closeSettings = { if (settingsOnly) close() else settings = false }
    if (settings) AlertDialog(onDismissRequest = closeSettings, title = { Text("Browser and add-on") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Bypass Paywalls Clean 4.4.9.6")
            Row {
                Text("Enable bundled add-on", Modifier.weight(1f))
                Switch(addon.enabled, onCheckedChange = model::configure, enabled = !addon.busy)
            }
            Text("This third-party add-on can read and change pages, cookies and requests on supported news websites opened in this browser. It has its own site and update options. Website support varies.")
            Text(if (addon.busy) "Applying add-on setting…" else if (addon.enabled) "Add-on enabled" else "Add-on off")
            addon.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(enabled = addon.enabled && model.ready && !addon.busy, onClick = { settings = false; if (!model.engine.clickAction(model.current?.session)) model.openOptions() }) { Text("Add-on site controls") }
            TextButton(enabled = addon.enabled && model.ready && !addon.busy, onClick = { settings = false; model.openOptions() }) { Text("Extension options") }
            TextButton(onClick = { settings = false; model.navigate("https://gitflic.ru/project/magnolia1234/bpc_uploads") }) { Text("Add-on project") }
            HorizontalDivider()
            Text("uBlock Origin 1.76.7.0")
            Row {
                Text("Enable ad blocker", Modifier.weight(1f))
                Switch(addon.adblockEnabled, onCheckedChange = model::configureAdblock, enabled = !addon.adblockBusy)
            }
            Text("Blocks ads and trackers on pages opened in this browser using uBlock Origin's bundled filter lists. Runs in the background; no toolbar button.")
            Text(if (addon.adblockBusy) "Applying ad blocker setting…" else if (addon.adblockEnabled) "Ad blocker enabled" else "Ad blocker off")
            addon.adblockError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(onClick = { licences = true }) { Text("Licences and source") }
        }
    }, confirmButton = { TextButton(onClick = closeSettings) { Text("Done") } })
    if (licences) {
        val text = remember { context.assets.open("notices/browser.txt").bufferedReader().use { it.readText() } }
        AlertDialog(onDismissRequest = { licences = false }, title = { Text("Licences and source") }, text = {
            Text(text, Modifier.verticalScroll(rememberScrollState()))
        }, confirmButton = { TextButton(onClick = { licences = false }) { Text("Done") } })
    }
    model.popup?.let { popup ->
        AlertDialog(onDismissRequest = model::dismissPopup, title = { Text("Bypass Paywalls Clean") }, text = {
            AndroidView(factory = { GeckoView(it).apply { setSession(popup) } }, onRelease = { it.releaseSession() }, modifier = Modifier.fillMaxWidth().height(360.dp))
        }, confirmButton = { TextButton(onClick = model::dismissPopup) { Text("Done") } })
    }
    model.permission?.let { (details, _) -> AlertDialog(onDismissRequest = { model.answerPermission(false) },
        title = { Text("Add-on requests more access") }, text = { Text(details.joinToString("\n"), Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = { model.answerPermission(true) }) { Text("Allow") } },
        dismissButton = { TextButton(onClick = { model.answerPermission(false) }) { Text("Deny") } }) }
}
