package com.rooknews.app.screens.feeds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.FeedCategory
import com.rooknews.domain.model.FeedSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsScreen(
    viewModel: FeedsViewModel = rememberFeedsViewModel()
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography
    val currentMessage by rememberUpdatedState(viewModel.syncMessage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feeds", style = typography.headlineMedium, color = colors.onBackground) },
                actions = {
                    IconButton(
                        onClick = { viewModel.onSync() },
                        enabled = !viewModel.isSyncing
                    ) {
                        if (viewModel.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = colors.primary
                            )
                        } else {
                            Icon(Icons.Filled.Sync, contentDescription = "Sync feeds")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                viewModel.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                viewModel.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(viewModel.error ?: "Error", style = typography.bodyMedium, color = colors.error)
                    }
                }
                else -> {
                    // Sync message
                    currentMessage?.let { msg ->
                        Snackbar(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            containerColor = colors.primaryContainer
                        ) {
                            Text(msg, style = typography.bodySmall, color = colors.primary)
                        }
                    }

                    // Filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = viewModel.selectedCategory == null,
                                onClick = { viewModel.onCategoryFilter(null) },
                                label = { Text("All", style = typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = colors.background,
                                    containerColor = colors.surfaceVariant,
                                    labelColor = colors.onSurface
                                )
                            )
                        }
                        items(FeedCategory.entries) { category ->
                            FilterChip(
                                selected = viewModel.selectedCategory == category,
                                onClick = { viewModel.onCategoryFilter(category) },
                                label = { Text(category.displayName, style = typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.primary,
                                    selectedLabelColor = colors.background,
                                    containerColor = colors.surfaceVariant,
                                    labelColor = colors.onSurface
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = colors.outlineVariant)

                    // Feed list
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(viewModel.filteredFeeds, key = { it.id }) { feed ->
                            FeedRow(feed = feed, onToggle = { viewModel.onToggleFeed(feed.id, !feed.enabled) })
                        }

                        if (viewModel.filteredFeeds.isEmpty() && viewModel.selectedCategory != null) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No feeds in this category", style = typography.bodyMedium, color = colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedRow(
    feed: FeedSource,
    onToggle: () -> Unit
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(feed.title, style = typography.titleMedium, color = if (feed.enabled) colors.onSurface else colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(feed.category.displayName, style = typography.labelSmall, color = colors.onSurfaceVariant)
        }
        Switch(
            checked = feed.enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedThumbColor = colors.primary, checkedTrackColor = colors.primaryContainer)
        )
    }
}
