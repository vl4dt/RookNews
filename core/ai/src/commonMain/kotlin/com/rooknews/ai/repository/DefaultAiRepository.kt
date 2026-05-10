package com.rooknews.ai.repository

import com.rooknews.ai.models.ModelManager
import com.rooknews.ai.pipeline.AiProcessingPipeline
import com.rooknews.domain.model.AiModelInfo
import com.rooknews.domain.model.AiProcessingResult
import com.rooknews.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow

class DefaultAiRepository(
    private val pipeline: AiProcessingPipeline,
    private val modelManager: ModelManager
) : AiRepository {

    override val modelsFlow: Flow<List<AiModelInfo>> = modelManager.modelStates

    override suspend fun getModels(): List<AiModelInfo> = modelManager.getAllModels()

    override suspend fun downloadModel(modelId: String): Flow<Double> = modelManager.downloadModel(modelId)

    override suspend fun activateModel(modelId: String): Boolean = modelManager.activateModel(modelId)

    override suspend fun summarizeArticle(articleId: String): AiProcessingResult =
        pipeline.summarizeArticle(articleId)

    override suspend fun analyzeBias(articleId: String): AiProcessingResult =
        pipeline.analyzeBias(articleId)

    override suspend fun generateBriefing(topic: String?): AiProcessingResult =
        pipeline.generateBriefing(topics = topic?.let { listOf(it) })

    override suspend fun deepResearch(query: String): AiProcessingResult =
        pipeline.deepResearch(query)
}
