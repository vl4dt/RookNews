package com.rooknews.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rooknews.app.navigation.Tab
import com.rooknews.app.screens.briefings.BriefingsScreen
import com.rooknews.app.screens.feeds.FeedsScreen
import com.rooknews.app.screens.feeds.rememberFeedsViewModel
import com.rooknews.app.screens.home.HomeScreen
import com.rooknews.app.screens.research.ResearchScreen
import com.rooknews.app.screens.settings.SettingsScreen
import com.rooknews.app.screens.article.ArticleDetailScreen
import com.rooknews.app.ui.theme.RookNewsTheme
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.Article
import com.rooknews.domain.model.ReadingMode
import com.rooknews.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    val preferencesRepo: PreferencesRepository = koinInject()
    var readingMode by remember { mutableStateOf(ReadingMode.LIGHT) }
    var preferredProvider by remember { mutableStateOf(AiProvider.LOCAL) }

    var retentionDays by remember { mutableIntStateOf(90) }

    LaunchedEffect(Unit) {
        preferencesRepo.preferencesFlow
            .catch { /* use default */ }
            .collect { prefs ->
                readingMode = prefs.readingMode
                preferredProvider = prefs.preferredProvider
                retentionDays = prefs.articleRetentionDays
            }
    }

    // Auto-cleanup old non-bookmarked articles on app start
    val articleRepo: com.rooknews.domain.repository.ArticleRepository = koinInject()
    LaunchedEffect(Unit) {
        val prefs = preferencesRepo.preferencesFlow
            .catch { emit(com.rooknews.domain.model.UserPreferences()) }
            .first()
        if (prefs.articleRetentionDays > 0) {
            val cutoff = System.currentTimeMillis() - (prefs.articleRetentionDays * 86_400_000L)
            articleRepo.deleteUnbookmarkedOldArticles(cutoff)
        }
    }

    RookNewsTheme(readingMode = readingMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = RookNewsTheme.colors.background,
            contentColor = RookNewsTheme.colors.onBackground
        ) {
            AppContent(
                preferredProvider = preferredProvider
            )
        }
    }
}

@Composable
private fun AppContent(
    preferredProvider: AiProvider
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var articleDetailId by remember { mutableStateOf<String?>(null) }
    val feedsViewModel = rememberFeedsViewModel()

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
                            icon = {
                                Text(tabIcon(tab), style = RookNewsTheme.typography.headlineMedium)
                            },
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
                        onArticleClicked = { article -> articleDetailId = article.id },
                        onSyncFeeds = { feedsViewModel.onSync() }
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
