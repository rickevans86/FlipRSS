package com.fliprss.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fliprss.app.data.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: LibraryViewModel = viewModel()
            val state by model.library.state.collectAsStateWithLifecycle()
            val dark = useDarkTheme(state.settings.theme)
            val oled = state.settings.theme == AppTheme.OLED
            LaunchedEffect(dark, oled) {
                enableEdgeToEdge(
                    statusBarStyle = if (oled) SystemBarStyle.dark(android.graphics.Color.BLACK) else SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
                    navigationBarStyle = if (oled) SystemBarStyle.dark(android.graphics.Color.BLACK) else SystemBarStyle.auto(0xE6FFFFFF.toInt(), 0x801B1B1B.toInt()) { dark },
                )
                if (android.os.Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = !oled
            }
            FlipTheme(state.settings.theme) { FlipApp(model) }
        }
    }
}
private val Red = Color(0xFFB83224)

@Composable private fun useDarkTheme(theme: AppTheme): Boolean =
    when (theme) { AppTheme.SYSTEM -> isSystemInDarkTheme(); AppTheme.LIGHT -> false; AppTheme.DARK, AppTheme.OLED -> true }

@Composable private fun FlipTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val dark = useDarkTheme(theme)
    val colors = appColors(theme, dark)
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable fun FlipApp(model: LibraryViewModel = viewModel()) {
    val local by model.localLibrary.state.collectAsStateWithLifecycle()
    val fresh by model.freshLibrary.state.collectAsStateWithLifecycle()
    val feeds by model.localFeeds.collectAsStateWithLifecycle()
    val directory by model.directory.state.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf("FlipRSS") }
    var topic by rememberSaveable { mutableStateOf<String?>(null) }
    var readerIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var seenFreshRevision by rememberSaveable { mutableIntStateOf(fresh.accountRevision) }
    var seenLocalRevision by rememberSaveable { mutableIntStateOf(local.accountRevision) }
    var initialPage by rememberSaveable { mutableIntStateOf(0) }
    var accountOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var speechSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var speechReturnToSettings by remember { mutableStateOf(false) }
    var feedOpen by remember { mutableStateOf(false) }
    var feedToServer by remember { mutableStateOf(false) }
    var feedInitialUrl by remember { mutableStateOf("") }
    var feedQuery by rememberSaveable { mutableStateOf("") }
    var feedCategory by rememberSaveable { mutableStateOf("All topics") }
    var feedPageSize by rememberSaveable(feedQuery, feedCategory) { mutableIntStateOf(20) }
    LaunchedEffect(directory.feeds) { feedPageSize = 20 }
    var website by remember { mutableStateOf("") }
    var manageFeeds by rememberSaveable { mutableStateOf(false) }
    var viewFeed by remember { mutableStateOf<LocalFeed?>(null) }
    var transferFeed by remember { mutableStateOf<LocalFeed?>(null) }
    var transferRevision by remember { mutableIntStateOf(0) }
    var markTarget by remember { mutableStateOf<MarkReadTarget?>(null) }
    val library = if (destination == "FreshRSS") model.freshLibrary else model.localLibrary
    val source = if (destination == "FreshRSS") fresh else local
    val state = if (destination == "Saved") combinedSaved(
        local.copy(articles = model.localLibrary.bookmarkedArticles()), fresh.copy(articles = model.freshLibrary.bookmarkedArticles()))
        else source.copy(settings = local.settings)
    val sourcePage = destination == "FreshRSS" || destination == "FlipRSS"
    val canMarkAllRead = sourcePage && !state.loading && !state.loadingMore && !state.restoring &&
        !state.markingAll && !state.addingFeed && !state.connecting && !state.sessionExpired && state.pending.isEmpty() &&
        state.scope.sectionId == topic && !state.scope.savedOnly && state.scope.unreadOnly == local.settings.hideRead
    val selected = state.visibleArticles
    val readerArticles = readerIds.mapNotNull(model::findArticle)
    val scroll = rememberLazyListState()
    val context = LocalContext.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(local.settings.showFeedSearch) { destination = availableDestination(destination, local.settings.showFeedSearch) }
    LaunchedEffect(fresh.accountRevision, local.accountRevision) {
        if (seenFreshRevision != fresh.accountRevision || seenLocalRevision != local.accountRevision) {
            seenFreshRevision = fresh.accountRevision; seenLocalRevision = local.accountRevision
            readerIds = emptyList(); topic = null
        }
    }
    LaunchedEffect(destination, topic, fresh.accountRevision, local.accountRevision) {
        scroll.scrollToItem(0)
        when (destination) {
            "FreshRSS" -> if (!fresh.demo) model.freshLibrary.selectScope(LibraryScope(topic))
            "FlipRSS" -> model.localLibrary.selectScope(LibraryScope(topic))
            "Search" -> model.directory.setEnabled(local.settings.showFeedSearch)
            "Saved" -> {
                model.localLibrary.selectScope(LibraryScope(savedOnly = true))
                if (!fresh.demo) model.freshLibrary.selectScope(LibraryScope(savedOnly = true))
            }
        }
    }
    LaunchedEffect(local.settings.hideEmptyCategories, state.visibleSections, state.loading, topic) {
        if (sourcePage && local.settings.hideEmptyCategories && !state.loading && topic != null && state.visibleSections.none { it.id == topic }) topic = null
    }
    LaunchedEffect(fresh.feedAddedRevision) {
        if (transferFeed != null && fresh.feedAddedRevision > transferRevision) {
            transferFeed = null; Toast.makeText(context, "Feed added to FreshRSS. Local feed kept.", Toast.LENGTH_LONG).show()
        }
    }
    val rows = selected.drop(1).chunked(2)
    val cards = buildMap<String, List<Article>> {
        selected.firstOrNull()?.let { put("story:${it.id}", listOf(it)) }
        rows.forEach { put("story-row:${it.first().id}", it) }
    }
    LaunchedEffect(destination, topic, manageFeeds, local.settings.markReadOnScroll, selected.map { it.id }) {
        if (sourcePage && local.settings.markReadOnScroll && !(destination == "FreshRSS" && fresh.demo)) {
            val tracker = ScrollReadTracker()
            snapshotFlow {
                Triple(scroll.layoutInfo.visibleItemsInfo.filter { it.key.toString() in cards }.associate { it.key.toString() to it.index },
                    scroll.firstVisibleItemIndex to scroll.firstVisibleItemScrollOffset, scroll.isScrollInProgress)
            }.collect { (visible, position, scrolling) ->
                tracker.observe(visible, position.first, position.second, scrolling).forEach { key ->
                    cards[key].orEmpty().forEach { model.owner(it).setRead(it.id, true) }
                }
            }
        }
    }
    fun open(article: Article) { initialPage = selected.indexOf(article); readerIds = selected.map { it.id } }
    fun addLocal(feed: PopularFeed) {
        model.localLibrary.clearFeedError(); feedInitialUrl = feed.url; feedToServer = false; feedOpen = true
    }
    fun refresh() { when (destination) {
        "Search" -> model.directory.refresh()
        "Saved" -> { model.localLibrary.refresh(); if (!fresh.demo) model.freshLibrary.refresh() }
        else -> library.refresh()
    } }
    if (readerArticles.isNotEmpty()) Reader(readerArticles, initialPage, state, model, onSpeechSettings = { speechReturnToSettings = false; speechSettingsOpen = true }) { readerIds = emptyList() }
    else Scaffold(bottomBar = {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            libraryDestinations(local.settings.showFeedSearch).forEach { item ->
                NavigationBarItem(selected = destination == item, onClick = { destination = item; topic = null },
                    icon = { Icon(when (item) { "Search" -> Icons.Outlined.Search; "Saved" -> Icons.Outlined.BookmarkBorder
                        "FreshRSS" -> Icons.Outlined.RssFeed; else -> Icons.Outlined.ViewDay }, null) }, label = { Text(item) })
            }
        }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("story-list"), state = scroll,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Masthead(onSettings = { settingsOpen = true }, onAddFeed = {
                    if (fresh.demo || fresh.sessionExpired) accountOpen = true else {
                        model.freshLibrary.clearFeedError(); feedInitialUrl = ""; feedToServer = true; feedOpen = true
                    }
                }, onRefresh = ::refresh, refreshEnabled = !state.loading && !state.restoring && !directory.updating,
                    addEnabled = !fresh.addingFeed && !fresh.connecting && !fresh.restoring, showAdd = destination == "FreshRSS")
                if (state.loading || state.restoring) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (sourcePage && !(destination == "FreshRSS" && fresh.demo)) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.toggleable(local.settings.hideRead, role = Role.Switch,
                        onValueChange = { model.updateSettings(local.settings.copy(hideRead = it)) }), verticalAlignment = Alignment.CenterVertically) {
                        Switch(local.settings.hideRead, null); Text("Hide read", Modifier.padding(start = 6.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { markTarget = library.requestMarkAllRead() }, enabled = canMarkAllRead) { Text("Mark all read") }
                }
            }
            if (destination == "Search") {
                feedSearchPage(directory, feedQuery, feedCategory, { feedQuery = it }, { feedCategory = it }, website, { website = it },
                    { model.directory.find(website) }, model.directory::refresh, !local.addingFeed && !local.restoring, ::addLocal,
                    feedPageSize, { feedPageSize += 20 })
            } else if (destination == "FreshRSS" && fresh.demo) item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("FreshRSS", style = MaterialTheme.typography.headlineMedium)
                    Text("Connect your server to read and sync its feeds. Local subscriptions are available in FlipRSS.")
                    fresh.loginError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = { accountOpen = true }, enabled = !fresh.restoring) { Text("Connect FreshRSS") }
                }
            } else {
                state.error?.let { message -> item { ProblemBanner(message, { model.localLibrary.dismissError(); model.freshLibrary.dismissError() }, ::refresh) } }
                state.notice?.let { message -> item { TextButton(onClick = library::dismissNotice) { Text(message + " · Dismiss") } } }
                if (destination == "FlipRSS") {
                    item { TextButton(onClick = { manageFeeds = !manageFeeds }) { Text(if (manageFeeds) "Hide local feeds" else "Manage local feeds (${feeds.size})") } }
                    if (manageFeeds) items(feeds, key = { "local-feed:${it.id}" }) { feed ->
                        var menu by remember { mutableStateOf(false) }
                        ListItem(headlineContent = { Text(feed.title) }, trailingContent = {
                            Box {
                                IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, "Actions for ${feed.title}") }
                                DropdownMenu(menu, { menu = false }) {
                                    DropdownMenuItem(text = { Text("View feed URL") }, onClick = { menu = false; viewFeed = feed })
                                    DropdownMenuItem(text = { Text("Copy feed URL") }, onClick = { menu = false; clipboard.setText(androidx.compose.ui.text.AnnotatedString(feed.url)); Toast.makeText(context, "Feed URL copied", Toast.LENGTH_SHORT).show() })
                                    DropdownMenuItem(text = { Text("Add to FreshRSS") }, onClick = { menu = false; model.freshLibrary.clearFeedError(); transferRevision = fresh.feedAddedRevision; transferFeed = feed })
                                }
                            }
                        })
                    }
                }
                if (sourcePage) item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(topic == null, { topic = null }, label = { Text("All stories") })
                        state.visibleSections.filterNot { it.feed }.forEach { section -> FilterChip(topic == section.id, { topic = section.id }, label = { Text(section.title) }) }
                    }
                }
                if (selected.isEmpty() && !state.loading) item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (destination == "Saved") "No saved stories yet." else if (local.settings.hideRead) "No unread stories here." else "No stories here yet.", style = MaterialTheme.typography.headlineSmall)
                        Text(if (destination == "Saved") "Bookmarks from FreshRSS and FlipRSS appear here, including read stories." else if (destination == "FlipRSS") "Find RSS feeds in Search or refresh your local feeds." else "Add server feeds with + or refresh FreshRSS.")
                    }
                }
                selected.firstOrNull()?.let { article -> item(key = "story:${article.id}") { StoryCard(article, true, state, model) { open(article) } } }
                items(rows.size, key = { "story-row:${rows[it].first().id}" }) { index ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rows[index].forEach { article -> Box(Modifier.weight(1f)) { StoryCard(article, false, state, model) { open(article) } } }
                        if (rows[index].size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                if (state.continuation != null) item {
                    OutlinedButton(onClick = { if (destination == "Saved") model.freshLibrary.loadMore() else library.loadMore() }, enabled = !state.loadingMore) { Text(if (state.loadingMore) "Loading…" else "Load more stories") }
                }
                if (sourcePage) item(key = "mark-all-read-footer") {
                    OutlinedButton(onClick = { markTarget = library.requestMarkAllRead() }, enabled = canMarkAllRead,
                        modifier = Modifier.fillMaxWidth().testTag("mark-all-read-footer")) {
                        Icon(Icons.Outlined.DoneAll, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.markingAll) "Marking…" else "Mark all read")
                    }
                }
            }
        }
    }
    if (settingsOpen) SettingsDialog(local.copy(account = fresh.account, sessionExpired = fresh.sessionExpired), model.localLibrary, onAccount = { settingsOpen = false; accountOpen = true },
        onClose = { settingsOpen = false }, onSettings = model::updateSettings,
        onSpeech = { speechReturnToSettings = true; settingsOpen = false; speechSettingsOpen = true })
    if (speechSettingsOpen) SpeechSettingsDialog(local.settings, model::updateSettings) { speechSettingsOpen = false; settingsOpen = speechReturnToSettings }
    if (accountOpen) AccountDialog(fresh, model.freshLibrary) { accountOpen = false }
    if (feedOpen) AddFeedDialog(if (feedToServer) fresh else local, if (feedToServer) model.freshLibrary else model.localLibrary,
        onAccount = { feedOpen = false; accountOpen = true }, onClose = { feedOpen = false }, initialUrl = feedInitialUrl)
    markTarget?.let { target -> AlertDialog(onDismissRequest = { markTarget = null }, title = { Text("Mark all read?") },
        text = { Text("Mark all stories in ${target.title} as read in $destination? Newer stories stay unread.") },
        confirmButton = { TextButton(onClick = { markTarget = null; library.markAllRead(target) }) { Text("Mark read") } },
        dismissButton = { TextButton(onClick = { markTarget = null }) { Text("Cancel") } }) }
    viewFeed?.let { feed -> AlertDialog(onDismissRequest = { viewFeed = null }, title = { Text(feed.title) }, text = {
        androidx.compose.foundation.text.selection.SelectionContainer { Text(feed.url) }
    }, confirmButton = { TextButton(onClick = { runCatching { uriHandler.openUri(feed.url) } }) { Text("Open feed") } },
        dismissButton = { TextButton(onClick = { viewFeed = null }) { Text("Close") } }) }
    if (!accountOpen) transferFeed?.let { feed -> AlertDialog(onDismissRequest = { if (!fresh.addingFeed) transferFeed = null }, title = { Text("Add to FreshRSS?") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Subscribe to ${feed.title} as ${fresh.account?.username ?: "a connected FreshRSS account"}. The local feed and saved stories stay intact.")
            Text(feed.url, style = MaterialTheme.typography.bodySmall)
            fresh.feedError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (fresh.addingFeed) LinearProgressIndicator(Modifier.fillMaxWidth())
        } }, confirmButton = {
            if (fresh.demo || fresh.sessionExpired) TextButton(onClick = { accountOpen = true }, enabled = !fresh.restoring) { Text("Connect FreshRSS") }
            else Button(enabled = !fresh.addingFeed, onClick = { model.freshLibrary.addFeed(feed.url) }) { Text("Subscribe") }
        }, dismissButton = { TextButton(enabled = !fresh.addingFeed, onClick = { transferFeed = null }) { Text("Cancel") } }) }
}

@Composable private fun Masthead(onSettings: () -> Unit, onAddFeed: () -> Unit, onRefresh: () -> Unit,
    refreshEnabled: Boolean, addEnabled: Boolean, showAdd: Boolean = true) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("F", Modifier.clip(RoundedCornerShape(4.dp)).background(Red).padding(horizontal = 9.dp, vertical = 2.dp),
                color = Color.White, fontWeight = FontWeight.Black, fontSize = 25.sp)
            Text("FlipRSS", Modifier.weight(1f).padding(start = 8.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            if (showAdd) IconButton(onClick = onAddFeed, enabled = addEnabled) { Icon(Icons.Outlined.Add, "Add feed") }
            IconButton(onClick = onRefresh, enabled = refreshEnabled) { Icon(Icons.Outlined.Refresh, "Refresh feeds") }
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "App settings") }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
    }
}
@Composable private fun Eyebrow(text: String) {
    Text(text, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}
@Composable private fun ProblemBanner(problem: String, dismiss: () -> Unit, retry: (() -> Unit)? = null) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(problem, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
            Row {
                retry?.let { TextButton(onClick = it) { Text("Retry") } }
                TextButton(onClick = dismiss) { Text("Dismiss") }
            }
        }
    }
}
@Composable private fun StoryCard(article: Article, hero: Boolean, state: LibraryState, library: LibraryViewModel, onOpen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.settings.showImages) Box(Modifier.fillMaxWidth().height(if (hero) 230.dp else 145.dp).clip(RoundedCornerShape(10.dp))) {
            Cover(article, Modifier.fillMaxSize().clickable(onClickLabel = "Read ${article.title}", onClick = onOpen))
            Surface(Modifier.align(Alignment.TopEnd).padding(8.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = .96f)) {
                SaveButton(article, state, library)
            }
            if (article.id in state.readIds) Surface(Modifier.align(Alignment.BottomStart).padding(8.dp), shape = RoundedCornerShape(4.dp)) {
                Row(Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Check, null, Modifier.size(12.dp)); Text(" Read", fontSize = 10.sp)
                }
            }
        }
        if (!state.settings.showImages) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically) {
            if (article.id in state.readIds) Text("Read", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
            SaveButton(article, state, library)
        }
        Column(Modifier.clickable(onClickLabel = "Read ${article.title}", onClick = onOpen), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Eyebrow(article.topic.uppercase())
            Text(article.title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium,
                fontSize = if (hero) 30.sp else 22.sp, lineHeight = if (hero) 34.sp else 26.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            if (hero) Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${if (article.local || article.demo) "FlipRSS" else "FreshRSS"} · ${article.source} · ${article.minutes} MIN READ", fontSize = 9.sp, lineHeight = 14.sp, letterSpacing = .4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable private fun SaveButton(article: Article, state: LibraryState, library: LibraryViewModel) {
    val saved = article.id in state.savedIds
    IconToggleButton(checked = saved, onCheckedChange = { library.owner(article).toggleSaved(article.id) },
        enabled = !state.markingAll && MutationKey(article.id, false) !in state.pending) {
        Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
            if (saved) "Unsave ${article.title}" else "Save ${article.title}",
            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}
@Composable private fun Reader(articles: List<Article>, initialPage: Int, state: LibraryState, library: LibraryViewModel, onSpeechSettings: () -> Unit = {}, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val pager = rememberPagerState(initialPage = initialPage.coerceIn(articles.indices)) { articles.size }
    val scope = rememberCoroutineScope()
    val speech = rememberArticleSpeech(articles[pager.currentPage].id, state.settings.speech)
    ReadingScreenAwake(speech.playing)
    val articleContent by library.articleContent.state.collectAsStateWithLifecycle()
    val currentArticle = articleContent.display(articles[pager.currentPage])
    LaunchedEffect(articles[pager.currentPage].contentKey(), state.settings.autoLoadFullArticles) {
        library.articleContent.open(articles[pager.currentPage], state.settings.autoLoadFullArticles)
    }
    DisposableEffect(library.articleContent) { onDispose { library.articleContent.close() } }
    LaunchedEffect(currentArticle.contentKey(), speech) { speech.stop() }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    LaunchedEffect(pager, state.settings.autoMarkRead, state.markingAll) {
        if (state.settings.autoMarkRead && !state.markingAll) snapshotFlow { pager.settledPage }.distinctUntilChanged().collect { library.owner(articles[it]).setRead(articles[it].id, true) }
    }
    Scaffold(topBar = {
        val current = articles[pager.currentPage]
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back to stories") }
            Text("READER", Modifier.weight(1f), fontSize = 11.sp, letterSpacing = 1.sp)
            TextButton(enabled = !state.markingAll && MutationKey(current.id, true) !in state.pending,
                onClick = { library.owner(current).setRead(current.id, current.id !in state.readIds) }) {
                Text(if (current.id in state.readIds) "Mark unread" else "Mark read")
            }
            SaveButton(current, state, library)
        }
    }, bottomBar = {
        Surface(shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(enabled = pager.currentPage > 0, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Previous story")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${pager.currentPage + 1} / ${articles.size}", style = MaterialTheme.typography.labelMedium)
                    Text("Swipe to turn the page", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(enabled = pager.currentPage < articles.lastIndex, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Next story")
                }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { SpeechControls(currentArticle, speech, onSpeechSettings) }
            state.error?.let { ProblemBanner(it, { library.localLibrary.dismissError(); library.freshLibrary.dismissError() }) }
            HorizontalPager(state = pager, key = { articles[it].id }, modifier = Modifier.weight(1f)) { page ->
                val original = articles[page]
                val article = articleContent.display(original)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    if (state.settings.showImages) Cover(article, Modifier.fillMaxWidth().height(260.dp))
                    Column(Modifier.widthIn(max = 720.dp).align(Alignment.CenterHorizontally).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Eyebrow("${article.topic.uppercase()} / ${article.source}")
                        Text(article.title, fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 40.sp)
                        if (article.demo && article.summary.isNotBlank()) Text(article.summary, style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${if (article.demo) "DEMO EDITORIAL" else article.source} · ${article.minutes} MIN READ", fontSize = 10.sp, letterSpacing = 1.sp)
                        HorizontalDivider()
                        if (!article.demo && article.originalUrl != null && page == pager.currentPage) {
                            val matched = articleContent.key == original.contentKey()
                            when {
                                matched && articleContent.loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("Loading full article…", style = MaterialTheme.typography.bodySmall)
                                }
                                matched && articleContent.expanded != null -> Text("Fuller article text loaded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                else -> Column {
                                    if (matched && articleContent.attempted) Text("No extra text available. Showing the feed version.", style = MaterialTheme.typography.bodySmall)
                                    TextButton(onClick = { library.articleContent.load() }, enabled = matched) {
                                        Text(if (matched && articleContent.attempted) "Retry full article" else "Load full article")
                                    }
                                }
                            }
                        }
                        article.paragraphs.ifEmpty { listOf("This feed includes only a headline. Open the original article to read more.") }.forEach {
                            Text(it, fontFamily = FontFamily.Serif, fontSize = state.settings.readerTextSize.sp,
                                lineHeight = (state.settings.readerTextSize * 1.55f).sp)
                        }
                        article.originalUrl?.let { url ->
                            OutlinedButton(onClick = {
                                runCatching { uriHandler.openUri(url) }.onFailure {
                                    Toast.makeText(context, "No browser is available to open this article.", Toast.LENGTH_SHORT).show()
                                }
                            }) { Text("Open original article") }
                        }
                        if (article.demo) Text("Original sample story for the FlipRSS prototype. Photography: Unsplash.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
@Composable private fun Cover(article: Article, modifier: Modifier = Modifier) {
    if (article.demo) {
        val resource = when (article.image) {
            "mountains" -> R.drawable.mountains; "interior" -> R.drawable.interior; "technology" -> R.drawable.technology
            "forest" -> R.drawable.forest; "ocean" -> R.drawable.ocean; else -> R.drawable.city
        }
        Image(painterResource(resource), null, modifier, contentScale = ContentScale.Crop)
    } else Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Article, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        article.imageUrl?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
    }
}
