package com.rooknews.app.screens.briefings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.Briefing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BriefingsScreen(
    viewModel: BriefingsViewModel = rememberBriefingsViewModel()
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Briefings", style = typography.headlineMedium, color = colors.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (!viewModel.isGenerating) viewModel.onGenerateBriefing() },
                containerColor = if (viewModel.isGenerating) colors.primary.copy(alpha = 0.5f) else colors.primary,
                contentColor = colors.background
            ) {
                if (viewModel.isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.background)
                } else {
                    Text("✦", style = typography.headlineSmall)
                }
            }
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
                    }
                }
            }
            viewModel.briefings.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", style = typography.headlineLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No briefings yet", style = typography.bodyLarge, color = colors.onSurface)
                        Text("Tap ✨ to generate your first briefing", style = typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.briefings, key = { it.id }) { briefing ->
                        BriefingCard(briefing = briefing, onDelete = { viewModel.onDeleteBriefing(briefing.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BriefingCard(briefing: Briefing, onDelete: () -> Unit) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* open briefing detail */ },
        colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    AssistChip(
                        onClick = { },
                        label = { Text(briefing.type.displayName, style = typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colors.primaryContainer,
                            labelColor = colors.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(briefing.title, style = typography.titleLarge, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onDelete) {
                    Text("🗑", style = typography.bodyMedium, color = colors.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                briefing.contentMarkdown.take(200) + if (briefing.contentMarkdown.length > 200) "..." else "",
                style = typography.bodyMedium,
                color = colors.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(formatBriefingTime(briefing.generatedAt), style = typography.labelSmall, color = colors.onSurfaceVariant)
                Text("${briefing.readingTimeMinutes} min read", style = typography.labelSmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

private fun formatBriefingTime(timestampMs: Long): String {
    val diff = System.currentTimeMillis() - timestampMs
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "older"
    }
}
