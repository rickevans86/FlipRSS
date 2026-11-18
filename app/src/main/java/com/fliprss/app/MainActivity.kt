package com.fliprss.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { FlipTheme { FlipApp() } }
    }
}

private val Ink = Color(0xFF242820)
private val Paper = Color(0xFFF7F5EF)
private val Red = Color(0xFFB83224)

@Composable
private fun FlipTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Color(0xFFFFB4A8), background = Color(0xFF191C18),
            surface = Color(0xFF191C18), surfaceVariant = Color(0xFF2B3029))
    } else {
        lightColorScheme(primary = Red, background = Paper, surface = Paper,
            onSurface = Ink, onBackground = Ink, surfaceVariant = Color(0xFFEAE8E0))
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
fun FlipApp(model: LibraryViewModel = viewModel()) {
    var destination by rememberSaveable { mutableStateOf("Today") }
    var topic by rememberSaveable { mutableStateOf<String?>(null) }
    // Freeze the reader queue so unsaving an article never shifts the current story.
    var readerIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var initialPage by rememberSaveable { mutableIntStateOf(0) }
    val libraryScroll = rememberLazyListState()
    LaunchedEffect(destination, topic) { libraryScroll.scrollToItem(0) }
    val selected = DemoLibrary.select(topic, destination == "Saved", model.savedIds.toSet())
    val readerArticles = readerIds.mapNotNull { id -> DemoLibrary.articles.find { it.id == id } }

    if (readerArticles.isNotEmpty()) {
        Reader(readerArticles, initialPage, model.savedIds.toSet(), model::toggleSaved,
            model::markRead, onClose = { readerIds = emptyList() })
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    listOf("Today", "Topics", "Saved").forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item; topic = null },
                            icon = {
                                Icon(when (item) {
                                    "Topics" -> Icons.Outlined.GridView
                                    "Saved" -> Icons.Outlined.BookmarkBorder
                                    else -> Icons.Outlined.ViewDay
                                }, contentDescription = null)
                            },
                            label = { Text(item) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant),
                        )
                    }
                }
            },
        ) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), state = libraryScroll,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item { Masthead() }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Eyebrow(if (destination == "Saved") "YOUR PERSONAL COLLECTION" else "A LITTLE CURIOSITY, EVERY DAY")
                        Text(when (destination) {
                            "Topics" -> "Follow your curiosity."
                            "Saved" -> "For a slower moment."
                            else -> "Your daily perspective."
                        }, fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 39.sp)
                        Text(when (destination) {
                            "Topics" -> "Find a new angle in your favourite subjects."
                            "Saved" -> "Good stories, kept close."
                            else -> "Stories to open your world. Take your time."
                        }, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("DEMO", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Sample stories · FreshRSS connection coming next",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (destination == "Topics") {
                    items(DemoLibrary.topics.size, key = { "topic-$it" }) { index ->
                        val name = DemoLibrary.topics[index]
                        val stories = DemoLibrary.articles.filter { it.topic == name }
                        Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                            .clickable { topic = name; destination = "Today" }) {
                            Cover(stories.first(), Modifier.fillMaxSize())
                            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = .7f)))))
                            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                                Text(name, color = Color.White, fontFamily = FontFamily.Serif, fontSize = 30.sp)
                                Text("${stories.size} sample stories", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    item {
                        Row(Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (listOf("All stories") + DemoLibrary.topics).forEach { name ->
                                FilterChip(selected = (topic ?: "All stories") == name,
                                    onClick = { topic = name.takeUnless { it == "All stories" } },
                                    label = { Text(name) })
                            }
                        }
                    }
                    if (selected.isEmpty()) {
                        item {
                            Column(Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Outlined.BookmarkBorder, contentDescription = null,
                                    modifier = Modifier.size(36.dp))
                                Text("A little space for a good story.", fontFamily = FontFamily.Serif,
                                    fontSize = 24.sp)
                                Text("Tap a bookmark on Today to save a story here.",
                                    style = MaterialTheme.typography.bodyMedium)
                                TextButton(onClick = { destination = "Today"; topic = null }) {
                                    Text("Explore stories")
                                }
                            }
                        }
                    } else {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Eyebrow(if (destination == "Saved") "SAVED FOR YOU" else topic?.uppercase() ?: "THE READING ROOM")
                                Text("${selected.size} stories", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        item {
                            val article = selected.first()
                            StoryCard(article, hero = true, saved = article.id in model.savedIds,
                                read = article.id in model.readIds, onSave = { model.toggleSaved(article.id) },
                                onOpen = {
                                    initialPage = 0
                                    readerIds = selected.map { it.id }
                                })
                        }
                        val rows = selected.drop(1).chunked(2)
                        items(rows.size, key = { "stories-${rows[it].first().id}" }) { index ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                rows[index].forEach { article ->
                                    Box(Modifier.weight(1f)) {
                                        StoryCard(article, hero = false, saved = article.id in model.savedIds,
                                            read = article.id in model.readIds,
                                            onSave = { model.toggleSaved(article.id) }, onOpen = {
                                                initialPage = selected.indexOf(article)
                                                readerIds = selected.map { it.id }
                                            })
                                    }
                                }
                                if (rows[index].size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                        item {
                            HorizontalDivider()
                            Text("YOU’RE ALL CAUGHT UP WITH THE DEMO.",
                                Modifier.fillMaxWidth().padding(top = 20.dp), fontSize = 10.sp,
                                letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Masthead() {
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("F", Modifier.clip(RoundedCornerShape(4.dp)).background(Red)
                    .padding(horizontal = 9.dp, vertical = 2.dp), color = Color.White,
                    fontWeight = FontWeight.Black, fontSize = 25.sp)
                Text("FlipRSS", fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
            }
            Text("THE DAILY EDIT", fontSize = 10.sp, letterSpacing = 1.5.sp)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(text, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.5.sp,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun StoryCard(article: Article, hero: Boolean, saved: Boolean, read: Boolean,
    onSave: () -> Unit, onOpen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().height(if (hero) 230.dp else 145.dp)
            .clip(RoundedCornerShape(10.dp))) {
            Cover(article, Modifier.fillMaxSize().clickable(onClickLabel = "Read ${article.title}", onClick = onOpen))
            Surface(Modifier.align(Alignment.TopEnd).padding(8.dp), shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = .96f)) {
                SaveButton(saved, article.title, onSave)
            }
            if (read) {
                Surface(Modifier.align(Alignment.BottomStart).padding(8.dp),
                    shape = RoundedCornerShape(4.dp)) {
                    Row(Modifier.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                        Text(" Read", fontSize = 10.sp)
                    }
                }
            }
        }
        Column(Modifier.clickable(onClickLabel = "Read ${article.title}", onClick = onOpen),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Eyebrow(article.topic.uppercase())
            Text(article.title, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Medium,
                fontSize = if (hero) 30.sp else 22.sp, lineHeight = if (hero) 34.sp else 26.sp,
                maxLines = 4, overflow = TextOverflow.Ellipsis)
            if (hero) Text(article.summary, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${article.source} · ${article.minutes} MIN READ", fontSize = 9.sp,
                lineHeight = 14.sp, letterSpacing = .4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SaveButton(saved: Boolean, title: String, onSave: () -> Unit) {
    IconToggleButton(checked = saved, onCheckedChange = { onSave() }) {
        Icon(if (saved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = if (saved) "Unsave $title" else "Save $title",
            tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun Reader(articles: List<Article>, initialPage: Int, savedIds: Set<String>,
    onSave: (String) -> Unit, onRead: (String) -> Unit, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val pager = rememberPagerState(initialPage = initialPage.coerceIn(articles.indices)) { articles.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager) {
        snapshotFlow { pager.settledPage }.distinctUntilChanged().collect { onRead(articles[it].id) }
    }
    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back to stories") }
                Text("THE READING ROOM", Modifier.weight(1f), fontSize = 11.sp, letterSpacing = 1.sp)
                SaveButton(articles[pager.currentPage].id in savedIds,
                    articles[pager.currentPage].title) { onSave(articles[pager.currentPage].id) }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 2.dp) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(enabled = pager.currentPage > 0, onClick = {
                        scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
                    }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Previous story") }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${pager.currentPage + 1} / ${articles.size}", style = MaterialTheme.typography.labelMedium)
                        Text("Swipe to turn the page", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(enabled = pager.currentPage < articles.lastIndex, onClick = {
                        scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                    }) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Next story") }
                }
            }
        },
    ) { padding ->
        HorizontalPager(state = pager, key = { articles[it].id }, modifier = Modifier.padding(padding)) { page ->
            val article = articles[page]
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Cover(article, Modifier.fillMaxWidth().height(260.dp))
                Column(Modifier.widthIn(max = 720.dp).align(Alignment.CenterHorizontally).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Eyebrow("${article.topic.uppercase()} / ${article.source}")
                    Text(article.title, fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 40.sp)
                    Text(article.summary, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("DEMO EDITORIAL · ${article.minutes} MIN READ", fontSize = 10.sp, letterSpacing = 1.sp)
                    HorizontalDivider()
                    article.paragraphs.forEach { paragraph ->
                        Text(paragraph, fontFamily = FontFamily.Serif, fontSize = 19.sp, lineHeight = 30.sp)
                    }
                    Text("Original sample story for the FlipRSS prototype. Photography: Unsplash.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun Cover(article: Article, modifier: Modifier = Modifier) {
    val resource = when (article.image) {
        "mountains" -> R.drawable.mountains
        "interior" -> R.drawable.interior
        "technology" -> R.drawable.technology
        "forest" -> R.drawable.forest
        "ocean" -> R.drawable.ocean
        else -> R.drawable.city
    }
    Image(painterResource(resource), contentDescription = null,
        modifier = modifier, contentScale = ContentScale.Crop)
}
