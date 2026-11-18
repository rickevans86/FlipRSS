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
            LaunchedEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(0xE6FFFFFF.toInt(), 0x801B1B1B.toInt()) { dark },
                )
            }
            FlipTheme(state.settings.theme) { FlipApp(model) }
        }
    }
}
private val Ink = Color(0xFF242820)
private val Paper = Color(0xFFF7F5EF)
private val Red = Color(0xFFB83224)

@Composable private fun useDarkTheme(theme: AppTheme): Boolean =
    when (theme) { AppTheme.SYSTEM -> isSystemInDarkTheme(); AppTheme.LIGHT -> false; AppTheme.DARK -> true }

@Composable private fun FlipTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val dark = useDarkTheme(theme)
    val colors = if (dark) darkColorScheme(primary = Color(0xFFFFB4A8), background = Color(0xFF191C18),
        surface = Color(0xFF191C18), surfaceVariant = Color(0xFF2B3029))
    else lightColorScheme(primary = Red, background = Paper, surface = Paper,
        onSurface = Ink, onBackground = Ink, surfaceVariant = Color(0xFFEAE8E0))
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable fun FlipApp(model: LibraryViewModel = viewModel()) {
    val library = model.library
    val state by library.state.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf("Today") }
    var topic by rememberSaveable { mutableStateOf<String?>(null) }
    var readerIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var initialPage by rememberSaveable { mutableIntStateOf(0) }
    var accountOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var feedOpen by rememberSaveable { mutableStateOf(false) }
    var markTarget by remember { mutableStateOf<MarkReadTarget?>(null) }
    var seenAccountRevision by rememberSaveable { mutableIntStateOf(state.accountRevision) }
    val libraryScroll = rememberLazyListState()
    LaunchedEffect(state.accountRevision) {
        if (seenAccountRevision != state.accountRevision) {
            seenAccountRevision = state.accountRevision; destination = "Today"; topic = null; readerIds = emptyList()
            feedOpen = false; markTarget = null
        }
    }
    LaunchedEffect(destination, topic) {
        libraryScroll.scrollToItem(0)
        library.selectScope(LibraryScope(topic, destination == "Saved"))
    }
    val selected = state.visibleArticles
    val readerArticles = readerIds.mapNotNull(library::findArticle)
    val sectionTitle = state.sections.find { it.id == topic }?.title
    val scopeMatchesView = state.scope.sectionId == topic && state.scope.savedOnly == (destination == "Saved") &&
        state.scope.unreadOnly == state.settings.hideRead
    fun open(article: Article) { initialPage = selected.indexOf(article); readerIds = selected.map { it.id } }

    if (readerArticles.isNotEmpty()) {
        Reader(readerArticles, initialPage, state, library) { readerIds = emptyList() }
    } else {
        Scaffold(bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf("Today", "Topics", "Saved").forEach { item ->
                    NavigationBarItem(selected = destination == item, onClick = { destination = item; topic = null },
                        icon = { Icon(when (item) {
                            "Topics" -> Icons.Outlined.GridView
                            "Saved" -> Icons.Outlined.BookmarkBorder
                            else -> Icons.Outlined.ViewDay
                        }, null) }, label = { Text(item) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.surfaceVariant))
                }
            }
        }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), state = libraryScroll,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item {
                    Masthead(onSettings = { settingsOpen = true }, onAddFeed = {
                        if (state.demo || state.sessionExpired) accountOpen = true
                        else { library.clearFeedError(); feedOpen = true }
                    }, onRefresh = library::refresh,
                        refreshEnabled = !state.demo && !state.loading && !state.restoring,
                        addEnabled = !state.addingFeed && !state.markingAll && !state.connecting)
                    if (state.loading || state.restoring || state.markingAll) LinearProgressIndicator(Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.toggleable(value = state.settings.hideRead, role = Role.Switch,
                            onValueChange = { library.updateSettings(state.settings.copy(hideRead = it)) }),
                            verticalAlignment = Alignment.CenterVertically) {
                            Switch(state.settings.hideRead, onCheckedChange = null)
                            Text("Hide read", Modifier.padding(start = 6.dp), style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { markTarget = library.markReadTarget() },
                            enabled = destination != "Topics" && scopeMatchesView && !state.loading && !state.loadingMore && !state.restoring &&
                                !state.markingAll && !state.addingFeed && !state.connecting && !state.sessionExpired && state.pending.isEmpty()) {
                            Text(if (state.markingAll) "Marking…" else "Mark all read")
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Eyebrow(if (destination == "Saved") "YOUR PERSONAL COLLECTION" else "A LITTLE CURIOSITY, EVERY DAY")
                        Text(when (destination) { "Topics" -> "Follow your curiosity."; "Saved" -> "For a slower moment."; else -> "Your daily perspective." },
                            fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 39.sp)
                        Text(when (destination) { "Topics" -> "Find a new angle in your favourite subjects."; "Saved" -> "Good stories, kept close."; else -> "Stories to open your world. Take your time." },
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Eyebrow(if (state.demo) "DEMO · SAMPLE STORIES" else "FRESHRSS · ${state.account?.username.orEmpty()}")
                            Text(if (state.demo) "Try the reading room, or connect your own feeds." else
                                if (state.sessionExpired) "Your saved session needs a new sign-in." else "${state.unread?.let { "$it unread · " }.orEmpty()}Read and saved changes sync to your account.",
                                style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { accountOpen = true }) {
                                Text(if (state.demo) "Connect FreshRSS" else if (state.sessionExpired) "Sign in again" else "Account")
                            }
                        }
                    }
                }
                state.notice?.let { notice -> item {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(notice, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = library::dismissNotice) { Text("Dismiss") }
                        }
                    }
                } }
                state.error?.let { problem -> item { ProblemBanner(problem, library::dismissError) { library.refresh() } } }
                if (destination == "Topics") {
                    val topics = state.sections.filterNot { it.feed }
                    items(topics, key = { it.id }) { section ->
                        Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant).clickable { topic = section.id; destination = "Today" }) {
                            val cover = library.cover(section)
                            if (cover != null && state.settings.showImages) Cover(cover, Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .75f)))))
                            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                                Text(section.title, color = Color.White, fontFamily = FontFamily.Serif, fontSize = 30.sp)
                                Text(if (state.demo) "${DemoLibrary.articles.count { it.topic == section.id }} sample stories"
                                    else section.unread?.let { "$it unread" } ?: "Open section", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                    val feeds = state.sections.filter { it.feed }
                    if (feeds.isNotEmpty()) item { Eyebrow("YOUR FEEDS") }
                    items(feeds, key = { it.id }) { feed ->
                        ListItem(headlineContent = { Text(feed.title) }, supportingContent = { Text(feed.unread?.let { "$it unread" } ?: "Open feed") },
                            leadingContent = { Icon(Icons.Outlined.RssFeed, null) }, modifier = Modifier.clickable { topic = feed.id; destination = "Today" })
                    }
                    if (state.sections.isEmpty() && !state.loading) item { Text("No sections or feeds loaded. Add subscriptions in FreshRSS, then refresh.") }
                } else {
                    if (destination != "Saved" || state.demo) item {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = topic == null, onClick = { topic = null }, label = { Text("All stories") })
                            state.sections.filterNot { it.feed }.forEach { section ->
                                FilterChip(selected = topic == section.id, onClick = { topic = section.id }, label = { Text(section.title) })
                            }
                        }
                    }
                    if (selected.isEmpty() && !state.loading) item {
                        Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(if (destination == "Saved") Icons.Outlined.BookmarkBorder else Icons.Outlined.RssFeed, null, Modifier.size(36.dp))
                            Text(if (state.settings.hideRead) "No unread stories here." else if (destination == "Saved") "A little space for a good story." else "No stories here yet.", fontFamily = FontFamily.Serif, fontSize = 24.sp)
                            Text(if (state.settings.hideRead) "Turn off Hide read to include read stories." else if (destination == "Saved") "Bookmark stories to find them here." else "Refresh your feeds or choose another section.",
                                style = MaterialTheme.typography.bodyMedium)
                            if (destination == "Saved") TextButton(onClick = { destination = "Today"; topic = null }) { Text("Explore stories") }
                        }
                    }
                    if (selected.isNotEmpty()) {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Eyebrow(if (destination == "Saved") "SAVED FOR YOU" else sectionTitle?.uppercase() ?: "THE READING ROOM")
                                Text("${selected.size} stories", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        item { StoryCard(selected.first(), true, state, library) { open(selected.first()) } }
                        val rows = selected.drop(1).chunked(2)
                        items(rows.size, key = { "stories-${rows[it].first().id}" }) { index ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                rows[index].forEach { article -> Box(Modifier.weight(1f)) { StoryCard(article, false, state, library) { open(article) } } }
                                if (rows[index].size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    if (state.continuation != null) item {
                        OutlinedButton(onClick = library::loadMore, enabled = !state.loading && !state.loadingMore, modifier = Modifier.fillMaxWidth()) {
                            Text(if (state.loadingMore) "Loading…" else "Load more stories")
                        }
                    } else if (selected.isNotEmpty() && !state.loading) item {
                        HorizontalDivider()
                        Text(if (state.demo) "YOU’RE ALL CAUGHT UP WITH THE DEMO." else "END OF THIS STREAM",
                            Modifier.fillMaxWidth().padding(top = 20.dp), fontSize = 10.sp, letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (accountOpen) AccountDialog(state, library) { accountOpen = false }
    if (settingsOpen) SettingsDialog(state, library,
        onAccount = { settingsOpen = false; accountOpen = true }, onClose = { settingsOpen = false })
    if (feedOpen) AddFeedDialog(state, library,
        onAccount = { feedOpen = false; accountOpen = true }, onClose = { feedOpen = false })
    markTarget?.let { target ->
        AlertDialog(onDismissRequest = { markTarget = null }, title = { Text("Mark all read?") },
            text = { Text("Mark all stories in “${target.title}” as read${if (state.demo) "" else " on FreshRSS, including stories not loaded yet"}? A timestamp cutoff keeps newer stories unread.") },
            confirmButton = { TextButton(onClick = { markTarget = null; library.markAllRead(target) }) { Text("Mark read") } },
            dismissButton = { TextButton(onClick = { markTarget = null }) { Text("Cancel") } })
    }
}

@Composable private fun Masthead(onSettings: () -> Unit, onAddFeed: () -> Unit, onRefresh: () -> Unit,
    refreshEnabled: Boolean, addEnabled: Boolean) {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("F", Modifier.clip(RoundedCornerShape(4.dp)).background(Red).padding(horizontal = 9.dp, vertical = 2.dp),
                color = Color.White, fontWeight = FontWeight.Black, fontSize = 25.sp)
            Text("FlipRSS", Modifier.weight(1f).padding(start = 8.dp), fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            IconButton(onClick = onAddFeed, enabled = addEnabled) { Icon(Icons.Outlined.Add, "Add feed") }
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
@Composable private fun StoryCard(article: Article, hero: Boolean, state: LibraryState, library: LibraryController, onOpen: () -> Unit) {
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
            Text("${article.source} · ${article.minutes} MIN READ", fontSize = 9.sp, lineHeight = 14.sp, letterSpacing = .4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable private fun SaveButton(article: Article, state: LibraryState, library: LibraryController) {
    val saved = article.id in state.savedIds
    IconToggleButton(checked = saved, onCheckedChange = { library.toggleSaved(article.id) },
        enabled = !state.markingAll && MutationKey(article.id, false) !in state.pending) {
        Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
            if (saved) "Unsave ${article.title}" else "Save ${article.title}",
            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}
@Composable private fun Reader(articles: List<Article>, initialPage: Int, state: LibraryState, library: LibraryController, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val pager = rememberPagerState(initialPage = initialPage.coerceIn(articles.indices)) { articles.size }
    val scope = rememberCoroutineScope()
    val speech = rememberArticleSpeech(articles[pager.currentPage].id)
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    LaunchedEffect(pager, state.settings.autoMarkRead, state.markingAll) {
        if (state.settings.autoMarkRead && !state.markingAll) snapshotFlow { pager.settledPage }.distinctUntilChanged().collect { library.setRead(articles[it].id, true) }
    }
    Scaffold(topBar = {
        val current = articles[pager.currentPage]
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back to stories") }
            Text("READER", Modifier.weight(1f), fontSize = 11.sp, letterSpacing = 1.sp)
            TextButton(enabled = !state.markingAll && MutationKey(current.id, true) !in state.pending,
                onClick = { library.setRead(current.id, current.id !in state.readIds) }) {
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
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { SpeechControls(articles[pager.currentPage], speech) }
            state.error?.let { ProblemBanner(it, library::dismissError) }
            HorizontalPager(state = pager, key = { articles[it].id }, modifier = Modifier.weight(1f)) { page ->
                val article = articles[page]
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    if (state.settings.showImages) Cover(article, Modifier.fillMaxWidth().height(260.dp))
                    Column(Modifier.widthIn(max = 720.dp).align(Alignment.CenterHorizontally).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Eyebrow("${article.topic.uppercase()} / ${article.source}")
                        Text(article.title, fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 40.sp)
                        if (article.demo && article.summary.isNotBlank()) Text(article.summary, style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${if (article.demo) "DEMO EDITORIAL" else article.source} · ${article.minutes} MIN READ", fontSize = 10.sp, letterSpacing = 1.sp)
                        HorizontalDivider()
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
