package com.fliprss.app

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fliprss.app.data.PopularFeed
import java.util.Locale

internal fun LazyListScope.feedSearchPage(state: FeedSearchState, query: String, category: String,
    onQuery: (String) -> Unit, onCategory: (String) -> Unit, website: String, onWebsite: (String) -> Unit,
    onFind: () -> Unit, onRefresh: () -> Unit, canAdd: Boolean, onAdd: (PopularFeed) -> Unit,
    pageSize: Int = 20, onLoadMore: () -> Unit = {}) {
    val words = query.trim().lowercase(Locale.ROOT).split(Regex("\\s+")).filter(String::isNotEmpty)
    val results = state.feeds.filter { (category == "All topics" || it.category == category) && words.all { word ->
        "${it.title} ${it.description} ${it.category} ${it.url}".lowercase(Locale.ROOT).contains(word)
    } }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Find feeds", style = MaterialTheme.typography.headlineMedium)
            Text("Everything added here goes to your local FlipRSS library.")
            OutlinedTextField(website, onWebsite, Modifier.fillMaxWidth(), label = { Text("Website or feed URL") }, placeholder = { Text("https://example.com") }, singleLine = true)
            Button(onClick = onFind, enabled = !state.finding && website.isNotBlank()) { Text(if (state.finding) "Finding feeds…" else "Find feeds") }
            state.discoveryMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
    feedCards(state.found, "found", canAdd, onAdd)
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Popular feeds", style = MaterialTheme.typography.headlineSmall)
            Text("Updated from Feeder and Feedspot lists. A bundled selection and cached results are available offline.", style = MaterialTheme.typography.bodySmall)
            val uri = androidx.compose.ui.platform.LocalUriHandler.current
            Row {
                com.fliprss.app.data.DirectoryImporter.sources.forEach { (name, url) ->
                    TextButton(onClick = { runCatching { uri.openUri(url) } }) { Text("$name list") }
                }
            }
            TextButton(onClick = onRefresh, enabled = !state.updating) { Text(if (state.updating) "Updating lists…" else "Refresh feed lists") }
            if (state.updated > 0) Text("Last updated: " + java.text.DateFormat.getDateTimeInstance().format(java.util.Date(state.updated)), style = MaterialTheme.typography.bodySmall)
            state.notice?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), label = { Text("Search popular feeds") }, singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) }, trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Outlined.Clear, "Clear feed search") }
                })
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (listOf("All topics") + state.feeds.map { it.category }.distinct()).forEach { value ->
                    FilterChip(value == category, { onCategory(value) }, label = { Text(value) })
                }
            }
            Text("Showing ${minOf(pageSize, results.size)} of ${results.size} feeds · Adds to FlipRSS", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (results.isEmpty()) item { Text("No feeds match. Try another name or topic, or choose All topics.") }
    feedCards(results.take(pageSize), "catalog", canAdd, onAdd)
    if (pageSize < results.size) item { OutlinedButton(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) { Text("Load more feeds") } }
}
private fun LazyListScope.feedCards(feeds: List<PopularFeed>, prefix: String, canAdd: Boolean, onAdd: (PopularFeed) -> Unit) {
    items(feeds, key = { "$prefix:${it.id}" }) { feed ->
        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(feed.category, style = MaterialTheme.typography.labelSmall)
                Text(feed.title, style = MaterialTheme.typography.titleLarge)
                Text(feed.description, style = MaterialTheme.typography.bodySmall)
                Text(feed.url, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(enabled = canAdd, onClick = { onAdd(feed) }) { Text("Add ${feed.title}") }
            }
        }
    }
}
