package com.rooknews.app.screens.home

import androidx.compose.runtime.*
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.Article
import com.rooknews.domain.repository.AiRepository
import com.rooknews.domain.repository.ArticleRepository
import com.rooknews.domain.repository.ArticleSort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class HomeViewModel(
    private val articleRepository: ArticleRepository,
    private val aiRepository: AiRepository,
    private val preferredProvider: AiProvider = AiProvider.LOCAL
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var articles by mutableStateOf<List<Article>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var sortBy by mutableStateOf(ArticleSort.NEWEST)
        private set

    init {
        loadArticles()
    }

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
        // Reload articles from the database
        loadArticles()
    }

    fun onSummarizeArticle(articleId: String) {
        scope.launch {
            try {
                val result = aiRepository.summarizeArticle(articleId, preferredProvider)
                if (result.success && result.output != null) {
                    // Update article with AI summary
                    val updatedArticles = articles.map { article ->
                        if (article.id == articleId) {
                            article.copy(aiSummary = result.output)
                        } else article
                    }
                    articles = updatedArticles
                    // Persist the summary
                    updatedArticles.find { it.id == articleId }?.let { updated ->
                        articleRepository.insertArticle(updated)
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Summarization failed"
            }
        }
    }
}
