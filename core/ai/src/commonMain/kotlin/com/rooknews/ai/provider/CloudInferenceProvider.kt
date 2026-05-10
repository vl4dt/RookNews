package com.rooknews.ai.provider

import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.AiProcessingResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CloudInferenceProvider(
    private val httpClient: HttpClient,
    private val apiKey: String
) : InferenceProvider {

    override val providerType: AiProvider = AiProvider.CLOUD
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = false }

    override suspend fun generateTextStream(
        prompt: String,
        maxTokens: Int
    ): Flow<String> = flow {
        val modelName = "gemini-2.0-flash"
        val url = "$baseUrl/models/$modelName:generateContent?key=$apiKey"

        val requestBody = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(
                maxOutputTokens = maxTokens,
                temperature = 0.7f
            )
        )

        try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(requestBody))
            }

            if (response.status.isSuccess()) {
                val result = response.body<GeminiResponse>()
                result.candidates?.firstOrNull()?.content?.parts?.forEach { part ->
                    emit(part.text ?: "")
                }
            } else {
                emit("Error: API returned ${response.status.value}")
            }
        } catch (e: Exception) {
            emit("Error: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun generateText(
        prompt: String,
        maxTokens: Int
    ): AiProcessingResult {
        val start = System.currentTimeMillis()
        val output = buildString {
            generateTextStream(prompt, maxTokens).collect { append(it) }
        }
        val elapsed = System.currentTimeMillis() - start

        return if (output.startsWith("Error:")) {
            AiProcessingResult(
                success = false,
                errorMessage = output,
                processingTimeMs = elapsed,
                provider = AiProvider.CLOUD
            )
        } else {
            AiProcessingResult(
                success = true,
                output = output,
                processingTimeMs = elapsed,
                provider = AiProvider.CLOUD
            )
        }
    }

    override suspend fun isReady(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE"
    }

    override suspend fun warmUp() {
        // No warmup needed for cloud
    }
}

// Gemini API request/response types
@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiGenerationConfig(
    val maxOutputTokens: Int? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val usageMetadata: GeminiUsageMetadata? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@Serializable
data class GeminiUsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null
)
