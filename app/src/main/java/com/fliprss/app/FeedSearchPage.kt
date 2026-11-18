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
import com.fliprss.app.data.FeedDirectory
import com.fliprss.app.data.PopularFeed

internal fun LazyListScope.feedSearchPage(query: String, category: String,
    onQuery: (String) -> Unit, onCategory: (String) -> Unit, canAdd: Boolean,
    source: String, onAdd: (PopularFeed) -> Unit) {
    val results = FeedDirectory.search(query, category)
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Find your next feed", style = MaterialTheme.typography.headlineMedium)
            Text("Explore a selection of well-known RSS feeds. Search by name or topic.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), label = { Text("Search popular feeds") },
                placeholder = { Text("Try science, Android or world news") }, singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Outlined.Clear, "Clear feed search") } })
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FeedDirectory.categories.forEach { value -> FilterChip(selected = value == category,
                    onClick = { onCategory(value) }, label = { Text(value) }) }
            }
            Text("${results.size} feeds · $source", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (results.isEmpty()) item {
        Text("No feeds match this search. Try another name or topic, or choose All topics.")
    }
    items(results, key = { "directory-${it.id}" }) { feed ->
        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(feed.category.uppercase(), style = MaterialTheme.typography.labelSmall)
                Text(feed.title, style = MaterialTheme.typography.titleLarge)
                Text(feed.description, style = MaterialTheme.typography.bodyMedium)
                Text(feed.url, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(enabled = canAdd, onClick = { onAdd(feed) }) { Text("Add ${feed.title}") }
            }
        }
    }
    item { Text("Search covers this curated directory, not the whole web. Publisher feeds may change or limit content. You can also paste any feed URL using +.",
        style = MaterialTheme.typography.bodySmall) }
}
