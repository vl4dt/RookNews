package com.rooknews.ai

import com.rooknews.ai.models.ModelManager
import com.rooknews.ai.pipeline.AiProcessingPipeline
import com.rooknews.ai.provider.CloudInferenceProvider
import com.rooknews.ai.provider.InferenceProvider
import com.rooknews.ai.provider.LocalInferenceProvider
import com.rooknews.ai.repository.DefaultAiRepository
import com.rooknews.domain.repository.AiRepository
import com.rooknews.domain.repository.ArticleRepository
import io.ktor.client.*
import org.koin.core.module.dsl.*
import org.koin.dsl.module

val aiModule = module {
    // Model management
    single { ModelManager() }

    // Inference providers
    factory<InferenceProvider> { LocalInferenceProvider() }
    factory<InferenceProvider> {
        CloudInferenceProvider(
            httpClient = get(),
            apiKey = "YOUR_API_KEY_HERE" // TODO: inject from settings / preferences
        )
    }

    // AI processing pipeline
    single { AiProcessingPipeline(localProvider = get(), articleRepository = get()) }

    // AI repository
    single<AiRepository> { DefaultAiRepository(pipeline = get(), modelManager = get()) }
}
