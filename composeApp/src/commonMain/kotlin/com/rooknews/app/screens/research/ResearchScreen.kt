package com.rooknews.app.screens.research

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rooknews.app.ui.theme.RookNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    viewModel: ResearchViewModel = rememberResearchViewModel()
) {
    val colors = RookNewsTheme.colors
    val typography = RookNewsTheme.typography

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Research", style = typography.headlineMedium, color = colors.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Input area
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ask a research question", style = typography.titleMedium, color = colors.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = viewModel.query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., What caused the 2008 financial crisis?", style = typography.bodyMedium) },
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colors.surfaceVariant,
                            unfocusedContainerColor = colors.surfaceVariant,
                            focusedIndicatorColor = colors.primary,
                            unfocusedIndicatorColor = colors.outline
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (viewModel.query.isNotBlank() || viewModel.result != null) {
                            TextButton(onClick = { viewModel.onClear() }) {
                                Text("Clear", style = typography.labelMedium, color = colors.onSurfaceVariant)
                            }
                        }
                        Button(
                            onClick = { viewModel.onResearch() },
                            enabled = viewModel.query.isNotBlank() && !viewModel.isResearching,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background)
                        ) {
                            if (viewModel.isResearching) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.background)
                            } else {
                                Text("Research", style = typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // Results area
            when {
                viewModel.isResearching -> {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Researching...", style = typography.bodyMedium, color = colors.onSurfaceVariant)
                        }
                    }
                }
                viewModel.error != null -> {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("⚠️ ${viewModel.error}", style = typography.bodyMedium, color = colors.error)
                    }
                }
                viewModel.result != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface, contentColor = colors.onSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Research Result", style = typography.titleMedium, color = colors.onSurface)
                                AssistChip(
                                    onClick = { },
                                    label = { Text(viewModel.result!!.provider.displayName, style = typography.labelSmall) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = colors.primaryContainer,
                                        labelColor = colors.primary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = colors.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                viewModel.result!!.output ?: "No result",
                                style = typography.bodyLarge,
                                color = colors.onSurface,
                                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Processed in ${viewModel.result!!.processingTimeMs}ms", style = typography.labelSmall, color = colors.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                else -> {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", style = typography.headlineLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Enter a question above", style = typography.bodyLarge, color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
