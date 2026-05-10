package com.rooknews.app.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.ReadingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = rememberSettingsViewModel()
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography
    var showResetCacheDialog by remember { mutableStateOf(false) }

    // Confirmation dialog for reset cache
    if (showResetCacheDialog) {
        AlertDialog(
            onDismissRequest = { showResetCacheDialog = false },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null)
            },
            title = { Text("Reset Cache") },
            text = {
                Text(
                    "This will delete all non-bookmarked articles. Bookmarked articles will be preserved. This action cannot be undone."
                )
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        viewModel.onResetCache()
                        showResetCacheDialog = false
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = colors.error)
                ) {
                    Text("Delete", color = colors.primary)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = typography.headlineMedium, color = colors.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance
            SettingsSection(title = "Appearance", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                ReadingMode.entries.forEach { mode ->
                    SettingsRadioRow(
                        title = mode.displayName,
                        description = when (mode) {
                            ReadingMode.LIGHT -> "White background, dark text"
                            ReadingMode.SEPIA -> "Warm parchment tones"
                            ReadingMode.DARK -> "Dark background, light text"
                        },
                        selected = viewModel.preferences.readingMode == mode,
                        onClick = { viewModel.onReadingModeChanged(mode) }
                    )
                }
            }

            // AI Preferences
            SettingsSection(title = "AI & Inference", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default provider", style = RookNewsTheme.typography.bodyLarge, color = RookNewsTheme.colors.onSurface)
                        Text("Choose between local (free) or cloud (Pro) AI", style = RookNewsTheme.typography.bodySmall, color = RookNewsTheme.colors.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = viewModel.preferences.preferredProvider == AiProvider.LOCAL,
                            onClick = { viewModel.onPreferredProviderChanged(AiProvider.LOCAL) },
                            label = { Text("Local", style = typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.background,
                                containerColor = colors.surfaceVariant,
                                labelColor = colors.onSurface
                            )
                        )
                        FilterChip(
                            selected = viewModel.preferences.preferredProvider == AiProvider.CLOUD,
                            onClick = { viewModel.onPreferredProviderChanged(AiProvider.CLOUD) },
                            label = { Text("Cloud", style = typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.background,
                                containerColor = colors.surfaceVariant,
                                labelColor = colors.onSurface
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = viewModel.preferences.cloudApiKey,
                    onValueChange = { viewModel.onCloudApiKeyChanged(it) },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("Enter your API key (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (viewModel.preferences.cloudApiKey.isNotBlank()) {
                            Text("✓", style = typography.labelSmall, color = colors.primary)
                        }
                    }
                )
                Text(
                    "Required for cloud AI. Get a key at aistudio.google.com",
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Sync & Cache Settings
            SettingsSection(title = "Sync & Cache", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingsTextRow(
                    title = "Sync interval",
                    description = "How often to fetch new articles",
                    value = "${viewModel.preferences.syncIntervalMinutes} min"
                )
                SettingsSwitchRow(
                    title = "Morning briefing",
                    description = "Generate a briefing at ${viewModel.preferences.briefingTime}",
                    checked = viewModel.preferences.briefingEnabled,
                    onCheckedChange = { viewModel.onBriefingEnabled(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                RetentionPeriodSelector(
                    selectedDays = viewModel.preferences.articleRetentionDays,
                    onSelected = { viewModel.onArticleRetentionChanged(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ResetCacheButton(
                    onClick = { showResetCacheDialog = true }
                )
            }

            // About
            SettingsSection(title = "About", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingsTextRow(title = "Version", description = null, value = "1.0.0")
                SettingsTextRow(title = "License", description = "MIT License", value = null)
                SettingsTextRow(
                    title = "Inference",
                    description = "Local: prompt-based analysis. Cloud: Gemini API.",
                    value = viewModel.preferences.preferredProvider.displayName
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Column(modifier = modifier) {
        Text(title, style = typography.labelLarge, color = colors.primary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
private fun SettingsRadioRow(
    title: String,
    description: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.RadioButton)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = typography.bodyLarge, color = colors.onSurface)
            description?.let { Text(it, style = typography.bodySmall, color = colors.onSurfaceVariant) }
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.onSurfaceVariant)
        )
    }
}

@Composable
private fun SettingsTextRow(
    title: String,
    description: String?,
    value: String?
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = typography.bodyLarge, color = colors.onSurface)
            description?.let { Text(it, style = typography.bodySmall, color = colors.onSurfaceVariant) }
        }
        value?.let { Text(it, style = typography.labelMedium, color = colors.onSurfaceVariant) }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = typography.bodyLarge, color = colors.onSurface)
            description?.let { Text(it, style = typography.bodySmall, color = colors.onSurfaceVariant) }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = colors.primary, checkedTrackColor = colors.primaryContainer)
        )
    }
}

@Composable
private fun RetentionPeriodSelector(
    selectedDays: Int,
    onSelected: (Int) -> Unit
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography
    val options = listOf(30, 60, 90, 180, 365)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Keep articles for", style = typography.bodyLarge, color = colors.onSurface)
        Text("Older articles are automatically removed", style = typography.bodySmall, color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { days ->
                val selected = selectedDays == days
                FilterChip(
                    selected = selected,
                    onClick = { onSelected(days) },
                    label = { Text("${days}d", style = typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary,
                        selectedLabelColor = colors.background,
                        containerColor = colors.surfaceVariant,
                        labelColor = colors.onSurfaceVariant
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

@Composable
private fun ResetCacheButton(
    onClick: () -> Unit
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.error
        )
    ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Reset Cache (keeps bookmarks)", style = typography.labelLarge)
    }
}
