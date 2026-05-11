package com.rooknews.ai.pipeline

import com.rooknews.ai.provider.CloudInferenceProvider
import com.rooknews.ai.provider.InferenceProvider
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.AiProcessingResult
import com.rooknews.domain.model.AiStreamState
import com.rooknews.domain.repository.AiRepository
import com.rooknews.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Orchestrates AI processing pipelines.
 * Routes requests to the appropriate InferenceProvider based on:
 * 1. User preference (LOCAL vs CLOUD)
 * 2. Feature capabilities (some features may require cloud)
 * 3. Model availability
 */
class AiProcessingPipeline(
    private val localProvider: InferenceProvider,
    private val cloudProvider: CloudInferenceProvider,
    private val articleRepository: ArticleRepository
) {

    // --- Non-streaming (returns result directly) ---

    suspend fun summarizeArticle(articleId: String, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult {
        val article = articleRepository.getArticleById(articleId)
            ?: return failedResult("Article not found: $articleId")
        val text = article.bodyMarkdown ?: article.summary
            ?: return failedResult("No article content")
        val prompt = buildSummarizationPrompt(article.title, text)
        return execute(prompt, preferredProvider, maxTokens = 1024)
    }

    suspend fun analyzeBias(articleId: String, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult {
        val article = articleRepository.getArticleById(articleId)
            ?: return failedResult("Article not found: $articleId")
        val text = article.bodyMarkdown ?: article.summary
            ?: return failedResult("No article content")
        val prompt = buildBiasAnalysisPrompt(text)
        return execute(prompt, preferredProvider, maxTokens = 512)
    }

    suspend fun classifyTopics(articleId: String, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult {
        val article = articleRepository.getArticleById(articleId)
            ?: return failedResult("Article not found: $articleId")
        val text = article.bodyMarkdown ?: article.summary
            ?: return failedResult("No article content")
        val prompt = buildTopicClassificationPrompt(text)
        return execute(prompt, preferredProvider, maxTokens = 256)
    }

    suspend fun generateBriefing(topics: List<String>? = null, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult {
        val articles = articleRepository.getArticlesFlow().first().take(10)
        if (articles.isEmpty()) return failedResult("No articles available for briefing")

        val articleSummaries = articles.joinToString("\n---\n") { art ->
            "**${art.title}**\n${art.summary ?: art.bodyMarkdown?.take(500) ?: "No summary"}"
        }

        val prompt = buildBriefingPrompt(topics, articleSummaries)
        return execute(prompt, preferredProvider, maxTokens = 4096)
    }

    suspend fun deepResearch(query: String, preferredProvider: AiProvider = AiProvider.LOCAL): AiProcessingResult {
        val prompt = buildResearchPrompt(query)
        return execute(prompt, preferredProvider, maxTokens = 8192)
    }

    // --- Streaming (returns Flow of AiStreamState) ---

    fun summarizeArticleStream(articleId: String, preferredProvider: AiProvider = AiProvider.LOCAL): Flow<AiStreamState> = flow {
        emit(AiStreamState.Loading)
        val article = articleRepository.getArticleById(articleId)
        if (article == null) { emit(AiStreamState.Error("Article not found")); return@flow }
        val text = article.bodyMarkdown ?: article.summary
        if (text == null) { emit(AiStreamState.Error("No content")); return@flow }
        val prompt = buildSummarizationPrompt(article.title, text)

        val start = System.currentTimeMillis()
        var fullText = ""
        emit(AiStreamState.Idle)

        val provider = selectProvider(preferredProvider)
        provider.generateTextStream(prompt, maxTokens = 1024).collect { token ->
            fullText += token
            emit(AiStreamState.Streaming(token, fullText))
        }

        val elapsed = System.currentTimeMillis() - start
        emit(AiStreamState.Complete(fullText, elapsed))
    }

    fun analyzeBiasStream(articleId: String, preferredProvider: AiProvider = AiProvider.LOCAL): Flow<AiStreamState> = flow {
        emit(AiStreamState.Loading)
        val article = articleRepository.getArticleById(articleId)
        if (article == null) { emit(AiStreamState.Error("Article not found")); return@flow }
        val text = article.bodyMarkdown ?: article.summary
        if (text == null) { emit(AiStreamState.Error("No content")); return@flow }
        val prompt = buildBiasAnalysisPrompt(text)

        val start = System.currentTimeMillis()
        var fullText = ""
        emit(AiStreamState.Idle)

        val provider = selectProvider(preferredProvider)
        provider.generateTextStream(prompt, maxTokens = 512).collect { token ->
            fullText += token
            emit(AiStreamState.Streaming(token, fullText))
        }

        val elapsed = System.currentTimeMillis() - start
        emit(AiStreamState.Complete(fullText, elapsed))
    }

    fun generateBriefingStream(topics: List<String>? = null, preferredProvider: AiProvider = AiProvider.LOCAL): Flow<AiStreamState> = flow {
        emit(AiStreamState.Loading)
        val articles = articleRepository.getArticlesFlow().first().take(10)
        if (articles.isEmpty()) {
            emit(AiStreamState.Error("No articles available"))
            return@flow
        }

        val articleSummaries = articles.joinToString("\n---\n") { art ->
            "**${art.title}**\n${art.summary ?: art.bodyMarkdown?.take(500) ?: "No summary"}"
        }
        val prompt = buildBriefingPrompt(topics, articleSummaries)

        val start = System.currentTimeMillis()
        var fullText = ""
        emit(AiStreamState.Idle)

        val provider = selectProvider(preferredProvider)
        provider.generateTextStream(prompt, maxTokens = 4096).collect { token ->
            fullText += token
            emit(AiStreamState.Streaming(token, fullText))
        }

        val elapsed = System.currentTimeMillis() - start
        emit(AiStreamState.Complete(fullText, elapsed))
    }

    fun deepResearchStream(query: String, preferredProvider: AiProvider = AiProvider.LOCAL): Flow<AiStreamState> = flow {
        emit(AiStreamState.Loading)
        val prompt = buildResearchPrompt(query)

        val start = System.currentTimeMillis()
        var fullText = ""
        emit(AiStreamState.Idle)

        val provider = selectProvider(preferredProvider)
        provider.generateTextStream(prompt, maxTokens = 8192).collect { token ->
            fullText += token
            emit(AiStreamState.Streaming(token, fullText))
        }

        val elapsed = System.currentTimeMillis() - start
        emit(AiStreamState.Complete(fullText, elapsed))
    }

    // --- Provider selection ---

    /**
     * Select the provider to use based on user preference and availability.
     * Falls back from local to cloud if local isn't ready and cloud is configured.
     */
    private fun selectProvider(preferred: AiProvider): InferenceProvider {
        return when (preferred) {
            AiProvider.LOCAL -> localProvider
            AiProvider.CLOUD -> cloudProvider
        }
    }

    /**
     * Execute a prompt against the selected provider.
     */
    private suspend fun execute(
        prompt: String,
        preferredProvider: AiProvider,
        maxTokens: Int
    ): AiProcessingResult {
        val provider = selectProvider(preferredProvider)
        return provider.generateText(prompt, maxTokens)
    }

    // --- Helpers ---

    private fun failedResult(message: String): AiProcessingResult {
        return AiProcessingResult(success = false, errorMessage = message)
    }

    // --- Prompt templates ---

    private fun buildSummarizationPrompt(title: String, text: String): String =
        """
        You are a news summarizer. Produce a concise 3-5 sentence summary of the following article.
        Focus on key facts, figures, and implications. Use neutral language.
        Do not add commentary or opinions.

        Title: $title

        Article:
        $text
        """.trimIndent()

    private fun buildBiasAnalysisPrompt(text: String): String =
        """
        Analyze the following news article for potential bias. Assess:
        1. Language tone (neutral, positive, negative, sensationalist)
        2. Source attribution (well-attributed, weak attribution, anonymous)
        3. Factual balance (balanced, one-sided, selective)
        4. Emotional manipulation (minimal, moderate, high)

        Return ONLY a JSON object:
        {
          "bias_score": 0.0_to_1.0,
          "tone": "description",
          "attribution": "description",
          "balance": "description",
          "manipulation": "description"
        }

        Article:
        $text
        """.trimIndent()

    private fun buildTopicClassificationPrompt(text: String): String =
        """
        Classify this news article into 3-5 relevant topics. Return only a comma-separated list of topic names.
        Use standard news categories like: Politics, Technology, Science, Business, Health, Sports, Culture, Environment, Education.

        Article:
        $text
        """.trimIndent()

    private fun buildBriefingPrompt(topics: List<String>?, articleText: String): String =
        """
        Generate a news briefing${if (topics != null) " covering: ${topics.joinToString(", ")}" else ""}.
        Format as structured Markdown with sections, bullet points, and key takeaways.
        Keep it concise — target 5-10 minute read.

        Source articles:
        $articleText
        """.trimIndent()

    private fun buildResearchPrompt(query: String): String =
        """
        Conduct deep research on: $query
        Provide comprehensive analysis with:
        - Historical context
        - Key stakeholders and perspectives
        - Current developments
        - Expert opinions
        - Data and statistics where relevant

        Structure as a detailed research report in Markdown.
        """.trimIndent()
}
