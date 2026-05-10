package com.rooknews.app.screens.home

import androidx.compose.runtime.*
import com.rooknews.domain.model.Article
import com.rooknews.domain.repository.AiRepository
import com.rooknews.domain.repository.ArticleRepository
import com.rooknews.domain.repository.ArticleSort
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberHomeViewModel(
    articleRepository: ArticleRepository = koinInject(),
    aiRepository: AiRepository = koinInject()
): HomeViewModel {
    val vm = remember { HomeViewModel(articleRepository, aiRepository) }
    LaunchedEffect(Unit) { vm.loadArticles() }
    return vm
}

class HomeViewModel(
    private val articleRepository: ArticleRepository,
    private val aiRepository: AiRepository
) {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())

    var articles by mutableStateOf<List<Article>>(emptyList())
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var sortBy by mutableStateOf(ArticleSort.NEWEST)

    fun loadArticles() {
        scope.launch {
            isLoading = true
            error = null
            articleRepository.getArticlesFlow(sortBy = sortBy)
                .catch { e ->
                    error = e.message ?: "Failed to load articles"
                    isLoading = false
                }
                .collect { list ->
                    articles = list
                    isLoading = false
                    error = null
                }
        }
    }

    fun onSortChanged(newSort: ArticleSort) {
        sortBy = newSort
        loadArticles()
    }

    fun onRefresh() {
        loadArticles()
    }

    fun onSummarizeArticle(articleId: String) {
        scope.launch {
            try {
                val result = aiRepository.summarizeArticle(articleId)
                if (result.success && result.output != null) {
                    // Update article with AI summary
                    val updatedArticles = articles.map { article ->
                        if (article.id == articleId) {
                            article.copy(aiSummary = result.output)
                        } else article
                    }
                    articles = updatedArticles
                }
            } catch (e: Exception) {
                error = e.message ?: "Summarization failed"
            }
        }
    }
}
