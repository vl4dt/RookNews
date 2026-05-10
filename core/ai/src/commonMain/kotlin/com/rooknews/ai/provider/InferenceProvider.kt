package com.rooknews.ai.provider

import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.AiProcessingResult
import kotlinx.coroutines.flow.Flow

interface InferenceProvider {
    val providerType: AiProvider

    suspend fun generateTextStream(prompt: String, maxTokens: Int = 2048): Flow<String>
    suspend fun generateText(prompt: String, maxTokens: Int = 2048): AiProcessingResult
    suspend fun isReady(): Boolean
    suspend fun warmUp()
}
