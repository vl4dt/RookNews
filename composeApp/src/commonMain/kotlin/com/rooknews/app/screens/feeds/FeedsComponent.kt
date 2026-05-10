package com.rooknews.app.screens.feeds

import androidx.compose.runtime.*
import com.rooknews.domain.model.FeedCategory
import com.rooknews.domain.model.FeedSource
import com.rooknews.domain.repository.FeedRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberFeedsViewModel(
    repository: FeedRepository = koinInject()
): FeedsViewModel {
    val vm = remember { FeedsViewModel(repository) }
    LaunchedEffect(Unit) { vm.loadFeeds() }
    return vm
}

class FeedsViewModel(
    private val repository: FeedRepository
) {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())

    var feeds by mutableStateOf<List<FeedSource>>(emptyList())
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var selectedCategory by mutableStateOf<FeedCategory?>(null)

    val filteredFeeds: List<FeedSource>
        get() = if (selectedCategory != null) feeds.filter { it.category == selectedCategory } else feeds

    fun loadFeeds() {
        scope.launch {
            isLoading = true
            error = null
            repository.getFeedsFlow()
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
        scope.launch { repository.toggleFeed(feedId, enabled) }
    }

    fun onCategoryFilter(category: FeedCategory?) {
        selectedCategory = category
    }
}
