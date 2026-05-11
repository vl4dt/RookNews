package com.rooknews.ai.repository

import com.rooknews.ai.models.ModelManager
import com.rooknews.ai.pipeline.AiProcessingPipeline
import com.rooknews.domain.model.AiModelInfo
import com.rooknews.domain.model.AiProcessingResult
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DefaultAiRepository(
    private val pipeline: AiProcessingPipeline,
    private val modelManager: ModelManager
) : AiRepository {

    override val modelsFlow: Flow<List<AiModelInfo>>
        get() = flowOf(modelManager.getAllModels())

    override suspend fun getModels(): List<AiModelInfo> = modelManager.getAllModels()

    override suspend fun downloadModel(modelId: String): Flow<Double> = modelManager.downloadModel(modelId)

    override suspend fun activateModel(modelId: String): Boolean = modelManager.activateModel(modelId)

    override suspend fun deactivateActiveModel() = modelManager.deactivateActiveModel()

    override suspend fun summarizeArticle(articleId: String, preferredProvider: AiProvider): AiProcessingResult =
        pipeline.summarizeArticle(articleId, preferredProvider)

    override suspend fun analyzeBias(articleId: String, preferredProvider: AiProvider): AiProcessingResult =
        pipeline.analyzeBias(articleId, preferredProvider)

    override suspend fun generateBriefing(topic: String?, preferredProvider: AiProvider): AiProcessingResult =
        pipeline.generateBriefing(topics = topic?.let { listOf(it) }, preferredProvider = preferredProvider)

    override suspend fun deepResearch(query: String, preferredProvider: AiProvider): AiProcessingResult =
        pipeline.deepResearch(query, preferredProvider)
}
