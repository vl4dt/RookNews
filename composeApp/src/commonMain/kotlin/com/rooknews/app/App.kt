package com.rooknews.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rooknews.app.navigation.Tab
import com.rooknews.app.screens.briefings.BriefingsScreen
import com.rooknews.app.screens.feeds.FeedsScreen
import com.rooknews.app.screens.home.HomeScreen
import com.rooknews.app.screens.research.ResearchScreen
import com.rooknews.app.screens.settings.SettingsScreen
import com.rooknews.app.screens.article.ArticleDetailScreen
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.Article
import com.rooknews.domain.model.ReadingMode
import com.rooknews.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.catch
import org.koin.compose.koinInject

@Composable
fun App() {
    val preferencesRepo: PreferencesRepository = koinInject()
    var readingMode by remember { mutableStateOf(ReadingMode.LIGHT) }

    LaunchedEffect(Unit) {
        preferencesRepo.preferencesFlow
            .catch { /* use default */ }
            .collect { prefs -> readingMode = prefs.readingMode }
    }

    RookNewsTheme(readingMode = readingMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = RookNewsTheme.colors.background,
            contentColor = RookNewsTheme.colors.onBackground
        ) {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var articleDetailId by remember { mutableStateOf<String?>(null) }

    val tabs = Tab.entries.toTypedArray()
    val selectedTab = tabs.getOrNull(selectedTabIndex) ?: Tab.HOME

    Scaffold(
        bottomBar = {
            if (articleDetailId == null) {
                NavigationBar(
                    containerColor = RookNewsTheme.colors.surface,
                    tonalElevation = 2.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            label = { Text(tab.title, style = RookNewsTheme.typography.labelSmall) },
                            icon = { Text(tabIcon(tab), style = RookNewsTheme.typography.headlineMedium) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RookNewsTheme.colors.primary,
                                selectedTextColor = RookNewsTheme.colors.primary,
                                unselectedIconColor = RookNewsTheme.colors.onSurfaceVariant,
                                unselectedTextColor = RookNewsTheme.colors.onSurfaceVariant,
                                indicatorColor = RookNewsTheme.colors.primaryContainer.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = RookNewsTheme.colors.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (articleDetailId != null) {
                ArticleDetailScreen(
                    articleId = articleDetailId!!,
                    onBack = { articleDetailId = null }
                )
            } else {
                when (selectedTab) {
                    Tab.HOME -> HomeScreen(
                        onArticleClicked = { article -> articleDetailId = article.id }
                    )
                    Tab.FEEDS -> FeedsScreen()
                    Tab.BRIEFINGS -> BriefingsScreen()
                    Tab.RESEARCH -> ResearchScreen()
                    Tab.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

private fun tabIcon(tab: Tab): String {
    return when (tab) {
        Tab.HOME -> "📰"
        Tab.FEEDS -> "📡"
        Tab.BRIEFINGS -> "🤖"
        Tab.RESEARCH -> "🔍"
        Tab.SETTINGS -> "⚙️"
    }
}
