package com.rooknews.ai

import com.rooknews.ai.models.ModelManager
import com.rooknews.ai.pipeline.AiProcessingPipeline
import com.rooknews.ai.provider.CloudInferenceProvider
import com.rooknews.ai.provider.InferenceProvider
import com.rooknews.ai.provider.LocalInferenceProvider
import com.rooknews.ai.repository.DefaultAiRepository
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.repository.AiRepository
import com.rooknews.domain.repository.ArticleRepository
import io.ktor.client.*
import org.koin.core.module.dsl.*
import org.koin.dsl.module

/**
 * AI Koin module.
 *
 * Provides:
 * - ModelManager for on-device model lifecycle
 * - LocalInferenceProvider (prompt-based analysis, no cinterop needed)
 * - CloudInferenceProvider (Gemini API, configurable via apiKey parameter)
 * - AiProcessingPipeline with provider routing
 * - DefaultAiRepository tying it all together
 *
 * The apiKey parameter should be injected from user preferences at app startup.
 * To override: call `getKoin().stop(); startKoin { ... aiModule(apiKey = "...") }`
 */
fun aiModule(apiKey: String = "YOUR_API_KEY_HERE") = module {
    // Model management
    single { ModelManager() }

    // Inference providers — LocalInferenceProvider is the primary (InferenceProvider interface)
    single<InferenceProvider> { LocalInferenceProvider() }
    factory { CloudInferenceProvider(get(), apiKey) }

    // AI processing pipeline with both providers
    single { AiProcessingPipeline(localProvider = get(), cloudProvider = get(), articleRepository = get()) }

    // AI repository
    single<AiRepository> { DefaultAiRepository(pipeline = get(), modelManager = get()) }
}
