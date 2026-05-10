package com.rooknews.app.screens.research

import androidx.compose.runtime.*
import com.rooknews.domain.model.AiProcessingResult
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.repository.AiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun rememberResearchViewModel(
    repository: AiRepository = koinInject()
): ResearchViewModel {
    return remember { ResearchViewModel(repository) }
}

data class ResearchResult(
    val output: String,
    val provider: AiProvider,
    val processingTimeMs: Long
)

class ResearchViewModel(
    private val repository: AiRepository
) {
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob())

    var query by mutableStateOf("")
    var isResearching by mutableStateOf(false)
    var result by mutableStateOf<ResearchResult?>(null)
    var error by mutableStateOf<String?>(null)

    fun onQueryChanged(newQuery: String) {
        query = newQuery
    }

    fun onResearch() {
        if (query.isBlank()) return
        isResearching = true
        error = null
        result = null
        scope.launch {
            try {
                val aiResult = repository.deepResearch(query)
                if (aiResult.success && aiResult.output != null) {
                    result = ResearchResult(
                        output = aiResult.output!!,
                        provider = aiResult.provider,
                        processingTimeMs = aiResult.processingTimeMs
                    )
                } else {
                    error = aiResult.errorMessage ?: "Research failed"
                }
            } catch (e: Exception) {
                error = e.message ?: "Research failed"
            } finally {
                isResearching = false
            }
        }
    }

    fun onClear() {
        query = ""
        result = null
        error = null
    }
}
