package com.rooknews.app.screens.article

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.Article
import com.rooknews.domain.repository.ArticleRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    onBack: () -> Unit
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography
    val repository: ArticleRepository = koinInject()
    val scope = rememberCoroutineScope()

    var article by remember { mutableStateOf<Article?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showAiSummary by remember { mutableStateOf(false) }

    LaunchedEffect(articleId) {
        article = repository.getArticleById(articleId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Article", style = typography.titleMedium, color = colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = typography.headlineSmall, color = colors.onBackground)
                    }
                },
                actions = {
                    article?.let { art ->
                        IconButton(onClick = {
                            scope.launch {
                                repository.toggleBookmark(art.id)
                                article = repository.getArticleById(art.id)
                            }
                        }) {
                            Text(
                                text = if (art.bookmarked) "★" else "☆",
                                style = typography.headlineSmall,
                                color = if (art.bookmarked) colors.primary else colors.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            article == null -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Article not found", style = typography.bodyLarge, color = colors.onSurfaceVariant)
                }
            }
            else -> {
                ArticleContent(
                    article = article!!,
                    showAiSummary = showAiSummary,
                    onToggleAiSummary = { showAiSummary = !showAiSummary },
                    modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
private fun ArticleContent(
    article: Article,
    showAiSummary: Boolean,
    onToggleAiSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(article.title, style = typography.headlineMedium, color = colors.onSurface)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            article.author?.let { Text(it, style = typography.bodySmall, color = colors.onSurfaceVariant) }
            Text(formatArticleDate(article.publishedAt), style = typography.bodySmall, color = colors.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = colors.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))

        // AI Summary
        article.aiSummary?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.3f), contentColor = colors.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🤖 AI Summary", style = typography.labelMedium)
                    IconButton(onClick = onToggleAiSummary) {
                        Text(if (showAiSummary) "▼" else "▶", style = typography.labelMedium)
                    }
                }
            }
            if (showAiSummary) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.15f), contentColor = colors.onSurface)
                ) {
                    Text(it, style = typography.bodyMedium, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bias indicator
        article.aiBiasScore?.let { score ->
            val label = when {
                score < 0.33 -> "Low bias"
                score < 0.66 -> "Medium bias"
                else -> "High bias"
            }
            AssistChip(
                onClick = { },
                label = { Text("⚖️ Bias: $label (${score.toInt()}%)", style = typography.labelSmall) },
                colors = AssistChipDefaults.assistChipColors(containerColor = colors.primaryContainer, labelColor = colors.primary),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Topics
        if (article.aiTopics.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                article.aiTopics.take(5).forEach { topic ->
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text(topic, style = typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = colors.surfaceVariant, labelColor = colors.onSurfaceVariant)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Body
        article.bodyMarkdown?.let {
            Text(it, style = typography.articleBody, color = colors.onSurface)
        } ?: article.summary?.let {
            Text(it, style = typography.bodyLarge, color = colors.onSurface)
        } ?: run {
            Text("No content available", style = typography.bodyMedium, color = colors.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun formatArticleDate(timestampMs: Long): String {
    val diff = System.currentTimeMillis() - timestampMs
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "older"
    }
}
