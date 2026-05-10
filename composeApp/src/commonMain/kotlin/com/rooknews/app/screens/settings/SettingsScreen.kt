package com.rooknews.app.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.ReadingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = rememberSettingsViewModel()
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

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

            SettingsSection(title = "Sync", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
            }

            SettingsSection(title = "AI & Models", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingsTextRow(title = "Active model", description = "On-device AI model", value = "None")
                SettingsTextRow(title = "Cloud AI", description = "Use cloud models (Pro)", value = "Free tier")
            }

            SettingsSection(title = "About", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingsTextRow(title = "Version", description = null, value = "1.0.0")
                SettingsTextRow(title = "License", description = "MIT License", value = null)
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
private fun SettingsTextRow(title: String, description: String?, value: String?) {
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
private fun SettingsSwitchRow(title: String, description: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
