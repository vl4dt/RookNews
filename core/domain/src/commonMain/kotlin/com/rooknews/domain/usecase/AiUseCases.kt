package com.rooknews.domain.usecase

import com.rooknews.domain.repository.AiRepository

class SummarizeArticleUseCase(
    private val repository: AiRepository
) {
    suspend operator fun invoke(articleId: String) = repository.summarizeArticle(articleId)
}

class AnalyzeBiasUseCase(
    private val repository: AiRepository
) {
    suspend operator fun invoke(articleId: String) = repository.analyzeBias(articleId)
}

class GenerateBriefingUseCase(
    private val repository: AiRepository
) {
    suspend operator fun invoke(topic: String? = null) = repository.generateBriefing(topic)
}

class DeepResearchUseCase(
    private val repository: AiRepository
) {
    suspend operator fun invoke(query: String) = repository.deepResearch(query)
}
