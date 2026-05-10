package com.rooknews.domain.repository

import com.rooknews.domain.model.AiModelInfo
import com.rooknews.domain.model.AiProcessingResult
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    val modelsFlow: Flow<List<AiModelInfo>>
    suspend fun getModels(): List<AiModelInfo>
    suspend fun downloadModel(modelId: String): Flow<Double>
    suspend fun activateModel(modelId: String): Boolean
    suspend fun summarizeArticle(articleId: String): AiProcessingResult
    suspend fun analyzeBias(articleId: String): AiProcessingResult
    suspend fun generateBriefing(topic: String? = null): AiProcessingResult
    suspend fun deepResearch(query: String): AiProcessingResult
}
