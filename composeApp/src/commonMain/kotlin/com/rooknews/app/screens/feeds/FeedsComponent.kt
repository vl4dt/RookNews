package com.rooknews.app.screens.feeds

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rooknews.data.rss.RssFetcher
import com.rooknews.data.rss.RssParser
import com.rooknews.domain.model.FeedCategory
import com.rooknews.domain.model.FeedSource
import com.rooknews.domain.repository.ArticleRepository
import com.rooknews.domain.repository.FeedRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberFeedsViewModel(
    feedRepository: FeedRepository = koinInject(),
    articleRepository: ArticleRepository = koinInject(),
    rssFetcher: RssFetcher = koinInject(),
    rssParser: RssParser = koinInject()
): FeedsViewModel {
    val vm = remember { FeedsViewModel(feedRepository, articleRepository, rssFetcher, rssParser) }
    LaunchedEffect(Unit) { vm.loadFeeds() }
    return vm
}

class FeedsViewModel(
    private val feedRepository: FeedRepository,
    private val articleRepository: ArticleRepository,
    private val rssFetcher: RssFetcher,
    private val rssParser: RssParser
) : ViewModel() {
    var feeds by mutableStateOf<List<FeedSource>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var selectedCategory by mutableStateOf<FeedCategory?>(null)
        private set
    var isSyncing by mutableStateOf(false)
        private set
    var syncMessage by mutableStateOf<String?>(null)
        private set
    var lastSyncTime by mutableStateOf<Long?>(null)
        private set

    init {
        loadFeeds()
    }

    val filteredFeeds: List<FeedSource>
        get() = if (selectedCategory != null) feeds.filter { it.category == selectedCategory } else feeds

    fun loadFeeds() {
        viewModelScope.launch {
            isLoading = true
            error = null
            feedRepository.getFeedsFlow()
                .catch { e ->
                    error = e.message ?: "Failed to load feeds"
                    isLoading = false
                }
                .collect { list ->
                    feeds = list
                    isLoading = false
                    error = null
                }
        }
    }

    fun onToggleFeed(feedId: String, enabled: Boolean) {
        viewModelScope.launch { feedRepository.toggleFeed(feedId, enabled) }
    }

    fun onCategoryFilter(category: FeedCategory?) {
        selectedCategory = category
    }

    /**
     * Sync all enabled feeds.
     * Fetches RSS from each enabled feed, parses articles, and inserts them into the database.
     */
    fun onSync() {
        viewModelScope.launch {
            isSyncing = true
            syncMessage = null
            try {
                val enabledFeeds = feeds.filter { it.enabled }
                var totalFetched = 0

                for (feed in enabledFeeds) {
                    try {
                        val xml = rssFetcher.fetchFeedXml(feed)
                        if (xml != null) {
                            val articles = rssParser.parse(xml, feed.id)
                            if (articles.isNotEmpty()) {
                                articleRepository.insertArticles(articles)
                                totalFetched += articles.size
                            }
                            // Update last fetched timestamp
                            feedRepository.updateFeedLastFetched(feed.id, System.currentTimeMillis())
                        }
                    } catch (_: Exception) {
                        // Skip failed feeds
                    }
                }

                syncMessage = if (totalFetched > 0) {
                    "Synced $totalFetched articles from ${enabledFeeds.size} feeds"
                } else {
                    "All feeds up to date"
                }
                lastSyncTime = System.currentTimeMillis()
                loadFeeds()
            } catch (e: Exception) {
                syncMessage = "Sync failed: ${e.message}"
            } finally {
                isSyncing = false
            }
        }
    }
}
