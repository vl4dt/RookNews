package com.rooknews.ai.provider

import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.AiProcessingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LocalInferenceProvider : InferenceProvider {
    override val providerType: AiProvider = AiProvider.LOCAL

    override suspend fun generateTextStream(prompt: String, maxTokens: Int): Flow<String> = flow {
        emit("[LOCAL] Stub response for prompt: $prompt")
    }

    override suspend fun generateText(prompt: String, maxTokens: Int): AiProcessingResult {
        val text = buildString {
            generateTextStream(prompt, maxTokens).collect { append(it) }
        }
        return AiProcessingResult(
            success = true,
            output = text,
            provider = AiProvider.LOCAL
        )
    }

    override suspend fun isReady(): Boolean = true
    override suspend fun warmUp() {}
}
