package com.rooknews.domain.repository

import com.rooknews.domain.model.AiModelInfo
import com.rooknews.domain.model.AiProcessingResult
import com.rooknews.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    val modelsFlow: Flow<List<AiModelInfo>>
    suspend fun getModels(): List<AiModelInfo>
    suspend fun downloadModel(modelId: String): Flow<Double>
    suspend fun activateModel(modelId: String): Boolean
    suspend fun deactivateActiveModel()

    suspend fun summarizeArticle(articleId: String, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult
    suspend fun analyzeBias(articleId: String, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult
    suspend fun generateBriefing(topic: String? = null, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult
    suspend fun deepResearch(query: String, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult
}
