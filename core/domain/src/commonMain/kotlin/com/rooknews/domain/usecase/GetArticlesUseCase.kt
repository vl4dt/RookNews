package com.rooknews.domain.usecase

import com.rooknews.domain.model.Article
import com.rooknews.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow

class GetArticlesUseCase(
    private val repository: ArticleRepository
) {
    operator fun invoke(
        feedSourceIds: List<String>? = null,
        unreadOnly: Boolean = false,
        bookmarkedOnly: Boolean = false
    ): Flow<List<Article>> {
        return repository.getArticlesFlow(feedSourceIds, unreadOnly, bookmarkedOnly)
    }
}
