package com.rooknews.app.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.Article
import com.rooknews.domain.repository.AiRepository
import com.rooknews.domain.repository.ArticleRepository
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = createHomeViewModel(),
    onArticleClicked: (Article) -> Unit,
    onSyncFeeds: () -> Unit = {}
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RookNews", style = typography.headlineMedium, color = colors.onBackground) },
                actions = {
                    // Refresh from local database (re-reads cached articles)
                    IconButton(onClick = { viewModel.onRefresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reload from cache")
                    }
                    // Sync button - fetches fresh content from all feeds
                    IconButton(onClick = { onSyncFeeds() }) {
                        Icon(Icons.Filled.CloudSync, contentDescription = "Sync feeds")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        when {
            viewModel.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            viewModel.error != null -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", style = typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(viewModel.error ?: "Error", style = typography.bodyMedium, color = colors.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(onClick = { viewModel.onRefresh() }) {
                            Text("Retry", style = typography.labelMedium)
                        }
                    }
                }
            }
            viewModel.articles.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📰", style = typography.headlineLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No articles yet", style = typography.bodyLarge, color = colors.onSurface)
                        Text("Tap the sync button in Feeds to get started", style = typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.articles, key = { it.id }) { article ->
                        ArticleCard(article = article, onClick = { onArticleClicked(article) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: Article, onClick: () -> Unit) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(article.title, style = typography.titleMedium, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                article.author?.let {
                    Text(it, style = typography.labelSmall, color = colors.onSurfaceVariant)
                }
                Text(formatRelativeTime(article.publishedAt), style = typography.labelSmall, color = colors.onSurfaceVariant)
                if (article.aiSummary != null) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Has AI summary", modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (article.bodyMarkdown != null) {
                Text(article.bodyMarkdown!!.take(200), style = typography.bodyMedium, color = colors.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            } else if (article.summary != null) {
                Text(article.summary!!, style = typography.bodyMedium, color = colors.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            } else if (article.author != null) {
                Text("By ${article.author}", style = typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun createHomeViewModel(): HomeViewModel {
    val articleRepository: ArticleRepository = koinInject()
    val aiRepository: AiRepository = koinInject()
    return remember { HomeViewModel(articleRepository, aiRepository) }
}

private fun formatRelativeTime(timestampMs: Long): String {
    val diff = System.currentTimeMillis() - timestampMs
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "older"
    }
}
