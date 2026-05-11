package com.rooknews.app.screens.research

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.AiProcessingResult
import com.rooknews.domain.repository.AiRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class ResearchResult(
    val output: String,
    val provider: AiProvider,
    val processingTimeMs: Long
)

@Composable
fun rememberResearchViewModel(
    repository: AiRepository = koinInject(),
    preferredProvider: AiProvider = AiProvider.LOCAL
): ResearchViewModel {
    return remember { ResearchViewModel(repository, preferredProvider) }
}

class ResearchViewModel(
    private val repository: AiRepository,
    private val preferredProvider: AiProvider = AiProvider.LOCAL
) : ViewModel() {
    var query by mutableStateOf("")
        private set
    var isResearching by mutableStateOf(false)
        private set
    var result by mutableStateOf<ResearchResult?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun onQueryChanged(newQuery: String) {
        query = newQuery
    }

    fun onResearch() {
        if (query.isBlank()) return
        isResearching = true
        error = null
        result = null
        viewModelScope.launch {
            try {
                val aiResult = repository.deepResearch(query, preferredProvider)
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
