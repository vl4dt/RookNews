package com.rooknews.app.screens.article

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.markdown.MarkdownText
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.Article
import com.rooknews.domain.repository.AiRepository
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
    val aiRepository: AiRepository = koinInject()
    val scope = rememberCoroutineScope()

    var article by remember { mutableStateOf<Article?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showAiSummary by remember { mutableStateOf(false) }
    var isBiasLoading by remember { mutableStateOf(false) }
    var biasResult by remember { mutableStateOf<String?>(null) }
    var biasError by remember { mutableStateOf<String?>(null) }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            Icon(
                                imageVector = if (art.bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = if (art.bookmarked) "Remove bookmark" else "Bookmark article",
                                tint = if (art.bookmarked) colors.primary else colors.onSurfaceVariant
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
                    onSummarize = {
                        scope.launch {
                            try {
                                val result = aiRepository.summarizeArticle(article!!.id)
                                if (result.success && result.output != null) {
                                    val updated = article!!.copy(aiSummary = result.output)
                                    article = updated
                                    repository.insertArticle(updated)
                                    showAiSummary = true
                                }
                            } catch (e: Exception) {
                                // Handle error (could show snackbar)
                            }
                        }
                    },
                    biasLoading = isBiasLoading,
                    biasResult = biasResult,
                    biasError = biasError,
                    onLoadBias = {
                        isBiasLoading = true
                        biasError = null
                        scope.launch {
                            try {
                                val result = aiRepository.analyzeBias(article!!.id)
                                if (result.success && result.output != null) {
                                    biasResult = result.output
                                } else {
                                    biasError = result.errorMessage ?: "Bias analysis failed"
                                }
                            } catch (e: Exception) {
                                biasError = e.message ?: "Bias analysis failed"
                            } finally {
                                isBiasLoading = false
                            }
                        }
                    },
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
    onSummarize: () -> Unit,
    biasLoading: Boolean,
    biasResult: String?,
    biasError: String?,
    onLoadBias: () -> Unit,
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

        // AI Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("🤖 AI Features", style = typography.labelMedium, color = colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onSummarize,
                        enabled = article.aiSummary.isNullOrBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Summarize", style = typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onLoadBias,
                        enabled = !biasLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (biasLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Bias Analysis", style = typography.labelMedium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Summary
        if (showAiSummary || article.aiSummary != null) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖 AI Summary", style = typography.labelMedium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
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
                    Text(
                        article.aiSummary ?: "No summary available",
                        style = typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bias analysis
        biasResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚖️ Bias Analysis", style = typography.labelMedium)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(result, style = typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        biasError?.let { error ->
            Text("⚠️ $error", style = typography.bodySmall, color = colors.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bias indicator (existing data)
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
            Text("Topics", style = typography.labelMedium, color = colors.primary, modifier = Modifier.padding(bottom = 4.dp))
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

        // Body — rendered with Markdown support
        // Try bodyMarkdown first (full article), then summary (description), then show a message
        article.bodyMarkdown?.let {
            MarkdownText(it, modifier = Modifier.padding(vertical = 8.dp))
        } ?: article.summary?.let {
            Text(it, style = typography.bodyLarge, color = colors.onSurface)
        } ?: run {
            // No article content — show helpful message with source link
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    "This article doesn't contain extractable content.",
                    style = typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
                article.originalUrl.takeIf { it.isNotBlank() }?.let { url ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Read the full article on the source website",
                        style = typography.bodySmall,
                        color = colors.primary
                    )
                }
            }
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
