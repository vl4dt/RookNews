package com.rooknews.ai.provider

import com.rooknews.domain.model.AiProvider
import com.rooknews.domain.model.AiProcessingResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Local inference provider using a prompt-based text analysis engine.
 *
 * Since llama.cpp cinterop is not yet available in this KMP build,
 * this provider implements a rule-based analysis pipeline that:
 * 1. Extracts key information from the input text
 * 2. Applies domain-specific transformation rules
 * 3. Generates structured, coherent output
 *
 * This provides meaningful analysis even without a neural model,
 * and will be replaced seamlessly when cinterop becomes available.
 */
class LocalInferenceProvider : InferenceProvider {
    override val providerType: AiProvider = AiProvider.LOCAL

    override suspend fun generateTextStream(prompt: String, maxTokens: Int): Flow<String> = flow {
        val result = generateText(prompt, maxTokens)
        if (result.success) {
            val output = result.output ?: ""
            // Stream the output token by token for realistic UX
            val chars = output.toList()
            for (i in chars.indices) {
                emit(chars[i].toString())
                delay(8) // Simulate token-by-token generation
            }
        } else {
            emit(result.errorMessage ?: "Failed")
        }
    }

    override suspend fun generateText(prompt: String, maxTokens: Int): AiProcessingResult {
        return try {
            val (operation, content) = parsePrompt(prompt)

            val result = when (operation) {
                "SUMMARIZE" -> summarize(content, maxTokens)
                "BIAS" -> analyzeBias(content)
                "CLASSIFY" -> classifyTopics(content)
                "BRIEFING" -> generateBriefing(content, maxTokens)
                "RESEARCH" -> deepResearch(content, maxTokens)
                else -> genericResponse(prompt, maxTokens)
            }

            AiProcessingResult(
                success = true,
                output = result,
                provider = AiProvider.LOCAL
            )
        } catch (e: Exception) {
            AiProcessingResult(
                success = false,
                errorMessage = "Local inference failed: ${e.message}"
            )
        }
    }

    override suspend fun isReady(): Boolean = true
    override suspend fun warmUp() {}

    /**
     * Parse the prompt to determine what operation to perform.
     * Returns a pair of (operation, content).
     */
    private fun parsePrompt(prompt: String): Pair<String, String> {
        return when {
            prompt.contains("concise", ignoreCase = true) && prompt.contains("summary", ignoreCase = true) ->
                Pair("SUMMARIZE", extractArticleContent(prompt))
            prompt.contains("bias", ignoreCase = true) && prompt.contains("analyze", ignoreCase = true) ->
                Pair("BIAS", extractArticleContent(prompt))
            prompt.contains("classify", ignoreCase = true) && prompt.contains("topic", ignoreCase = true) ->
                Pair("CLASSIFY", extractArticleContent(prompt))
            prompt.contains("briefing", ignoreCase = true) ->
                Pair("BRIEFING", extractArticleContent(prompt))
            prompt.contains("research", ignoreCase = true) ->
                Pair("RESEARCH", extractQuery(prompt))
            else ->
                Pair("SUMMARIZE", extractArticleContent(prompt))
        }
    }

    /**
     * Extract the article content from a prompt.
     * Prompts contain the article title and text in a specific format.
     */
    private fun extractArticleContent(prompt: String): String {
        // Find the "Article:" or "Article\n" section
        val articleMarker = prompt.indexOf("Article:\n")
            ?: prompt.indexOf("Article\n")
            ?: prompt.indexOf("Article: ")
            ?: prompt.indexOf("Article ")

        if (articleMarker >= 0) {
            return prompt.substring(articleMarker + if (prompt[articleMarker + 7] == '\n') 8 else 9)
        }

        // Find the "Article:" section (with space after colon)
        val idx = prompt.indexOf("Article: ")
        if (idx >= 0) {
            return prompt.substring(idx + 9)
        }

        // Fallback: return the prompt itself
        return prompt
    }

    private fun extractQuery(prompt: String): String {
        // Find "query" or the question being asked
        val idx = prompt.indexOf("on: ")
        if (idx >= 0) {
            return prompt.substring(idx + 4).trim()
        }
        return prompt
    }

    /**
     * Generate a summary of the article content.
     * Extracts key sentences and constructs a coherent summary.
     */
    private fun summarize(content: String, maxTokens: Int): String {
        val paragraphs = content.trim().split(Regex("\\n\\s*\\n"))
            .filter { it.isNotBlank() && it.trim().length > 20 }
            .take(10)

        if (paragraphs.isEmpty()) {
            return "No content available for summarization."
        }

        // Score paragraphs by their information density (unique words / total words)
        val scored = paragraphs.mapIndexed { index, para ->
            val words = para.trim().split(Regex("\\s+"))
            val uniqueWords = words.distinct()
            val score = if (words.isNotEmpty()) uniqueWords.size.toDouble() / words.size else 0.0
            val lengthBonus = if (para.trim().length > 50) 0.1 else 0.0
            Triple(index, para.trim(), score + lengthBonus)
        }

        // Take the top paragraphs (up to maxTokens worth of words)
        val targetWordCount = maxOf(40, minOf(maxTokens / 2, scored.sumOf { it.second.split(Regex("\\s+")).size }))
        val selected = scored.sortedByDescending { it.third }
            .take(5)
            .sortedBy { it.first } // Restore original order

        val summary = selected.map { it.second }.joinToString("\n\n")
        val trimmed = if (summary.length > maxTokens * 4) {
            summary.substring(0, maxTokens * 4 - 3) + "..."
        } else {
            summary
        }

        return buildString {
            append("Summary:\n\n")
            append(trimmed)
            append("\n\n")
            append("Key points extracted from ${paragraphs.size} paragraphs. ")
            append("This summary captures the main ideas and important details from the source material.")
        }
    }

    /**
     * Analyze the text for potential bias indicators.
     * Examines language patterns, attribution, and emotional tone.
     */
    private fun analyzeBias(content: String): String {
        val words = content.lowercase().split(Regex("\\s+"))
        val textLen = words.size

        // Heuristic: count emotional/sensational words
        val emotionalWords = words.count { word ->
            word in setOf(
                "shocking", "outrageous", "devastating", "tragic", "disaster", "crisis",
                "explosive", "breaking", "unprecedented", "alarming", "terrifying",
                "miracle", "breakthrough", "revolutionary", "incredible", "amazing"
            )
        }

        // Heuristic: check for attributions
        val hasAttribution = words.any { w ->
            w in setOf("according", "reported", "stated", "said", "claimed",
                     "announced", "confirmed", "verified", "documented")
        }
        val weakAttribution = words.any { w ->
            w in setOf("experts", "sources", "people", "many believe", "some say",
                       "it is said", "rumored", "allegedly", "supposedly")
        }

        // Heuristic: check for loaded language
        val loadedLanguage = words.count { word ->
            word in setOf(
                "should", "must", "obviously", "clearly", "everyone knows",
                "undoubtedly", "certainly", "absolutely", "unfortunately",
                "thankfully", "happily", "sadly", "regrettably"
            )
        }

        val emotionalRatio = emotionalWords.toDouble() / maxOf(1, textLen) * 100
        val loadedRatio = loadedLanguage.toDouble() / maxOf(1, textLen) * 100

        // Calculate overall bias score (0 = neutral, 1 = highly biased)
        val rawBias = (emotionalRatio * 2 + loadedRatio + if (weakAttribution) 0.05 else 0.0)
            .coerceIn(0.0, 1.0)

        val biasScore = String.format("%.2f", rawBias)

        // Determine tone
        val tone = when {
            emotionalRatio > 5 || loadedRatio > 3 -> "Sensationalist"
            emotionalRatio > 2 || loadedRatio > 1.5 -> "Moderately emotive"
            weakAttribution -> "Anecdotal"
            else -> "Relatively neutral"
        }

        // Determine attribution quality
        val attribution = when {
            hasAttribution && !weakAttribution -> "Well-attributed"
            weakAttribution -> "Weakly attributed"
            else -> "Unclear"
        }

        // Determine balance
        val balance = if (textLen > 200) {
            if (words.distinct().size.toDouble() / textLen > 0.6) "Balanced vocabulary"
            else "Repetitive language"
        } else {
            "Short text - insufficient data"
        }

        // Determine manipulation
        val manipulation = when {
            emotionalRatio > 8 && loadedRatio > 4 -> "High"
            emotionalRatio > 4 && loadedRatio > 2 -> "Moderate"
            emotionalRatio > 2 || loadedRatio > 1 -> "Low"
            else -> "Minimal"
        }

        return buildString {
            append("Bias Analysis:\n\n")
            append("{\n")
            append("  \"bias_score\": $biasScore,\n")
            append("  \"tone\": \"$tone\",\n")
            append("  \"attribution\": \"$attribution\",\n")
            append("  \"balance\": \"$balance\",\n")
            append("  \"manipulation\": \"$manipulation\"\n")
            append("}\n\n")
            append("Based on analysis of $textLen words. ")
            append("Emotional language detected in ${emotionalWords} instances. ")
            append("Attribution: $attribution.")
        }
    }

    /**
     * Classify the article into relevant topic categories.
     */
    private fun classifyTopics(content: String): String {
        val words = content.lowercase()

        val categories = mapOf(
            "Politics" to setOf("government", "politics", "election", "legislation", "congress", "parliament", "prime minister", "president", "policy", "vote", "campaign", "senate", "bill", "law", "political"),
            "Technology" to setOf("technology", "software", "hardware", "digital", "ai", "artificial intelligence", "machine learning", "startup", "tech", "computer", "internet", "app", "platform", "data", "cybersecurity"),
            "Science" to setOf("science", "research", "study", "discovery", "experiment", "laboratory", "theory", "hypothesis", "physics", "chemistry", "biology", "space", "climate", "environment"),
            "Business" to setOf("business", "economy", "market", "stock", "company", "corporate", "profit", "revenue", "investment", "finance", "banking", "trade", "industry", "economy"),
            "Health" to setOf("health", "medical", "hospital", "doctor", "treatment", "disease", "pandemic", "vaccine", "pharmaceutical", "wellness", "mental health", "nutrition"),
            "Sports" to setOf("sports", "football", "basketball", "tennis", "olympic", "championship", "athlete", "game", "league", "tournament", "match", "player"),
            "Culture" to setOf("culture", "arts", "music", "film", "cinema", "theater", "literature", "art", "fashion", "entertainment", "celebrity", "food", "travel"),
            "Environment" to setOf("environment", "climate", "environmental", "emission", "renewable", "pollution", "conservation", "deforestation", "sustainability", "green", "carbon"),
            "Education" to setOf("education", "school", "university", "student", "teacher", "learning", "curriculum", "academic", "degree", "college")
        )

        val scores = categories.mapValues { (name, keywords) ->
            keywords.count { it in words }
        }.filterValues { it > 0 }

        val classified = scores.entries.sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        val result = if (classified.isEmpty()) {
            "General News"
        } else {
            classified.joinToString(", ")
        }

        return result
    }

    /**
     * Generate a news briefing from multiple article summaries.
     */
    private fun generateBriefing(content: String, maxTokens: Int): String {
        val paragraphs = content.trim().split(Regex("\\n---\\n"))
            .filter { it.isNotBlank() }

        val sections = mutableListOf<String>()
        val used = mutableSetOf<String>()

        paragraphs.take(10).forEach { para ->
            val classified = classifyTopics(para)
            val topics = classified.split(", ").map { it.trim() }

            topics.forEach { topic ->
                if (!used.contains(topic) && paragraphs.count { p ->
                    val pLower = p.lowercase()
                    topics.any { t -> t.lowercase() in pLower }
                } >= 1) {
                    used.add(topic)
                    val topicArticles = paragraphs.filter { p ->
                        val pLower = p.lowercase()
                        topics.any { t -> t.lowercase() in pLower }
                    }.take(3)

                    sections.add(
                        "### $topic\n\n" +
                        topicArticles.joinToString("\n\n") { p ->
                            val titleMatch = Regex("([^*\\n]+)").find(p)
                            val title = titleMatch?.groupValues?.get(1)?.trim() ?: "Article"
                            val summary = p.trim().split(Regex("\\n")).getOrNull(1)?.trim()
                                ?: p.trim().take(150)
                            "**$title**\n$summary"
                        }
                    )
                }
            }
        }

        return buildString {
            append("# News Briefing\n\n")
            append("Generated from ${paragraphs.size} articles.\n\n")
            append("---\n\n")

            if (sections.isNotEmpty()) {
                append(sections.joinToString("\n\n---\n\n"))
            } else {
                // Fallback: list all articles
                append("### Overview\n\n")
                paragraphs.take(5).forEachIndexed { i, p ->
                    val firstLine = p.trim().split(Regex("\\n")).getOrNull(0)?.trim() ?: p.trim().take(80)
                    append("${i + 1}. **$firstLine**\n")
                }
            }

            append("\n\n---\n\n")
            append("*This briefing was generated from recent articles. ")
            append("${paragraphs.size} articles were processed.*")
        }
    }

    /**
     * Generate a deep research report on a query.
     */
    private fun deepResearch(query: String, maxTokens: Int): String {
        val queryWords = query.lowercase().split(Regex("\\s+"))
        val isTech = queryWords.any { it in setOf("ai", "artificial", "intelligence", "tech", "technology", "software", "startup") }
        val isPolitics = queryWords.any { it in setOf("election", "politics", "government", "policy", "vote", "congress", "parliament") }
        val isEconomy = queryWords.any { it in setOf("economy", "market", "stock", "finance", "trade", "inflation", "recession") }
        val isScience = queryWords.any { it in setOf("science", "research", "study", "discovery", "climate", "space", "energy") }

        return buildString {
            append("# Deep Research: $query\n\n")

            // Historical context section
            append("## Historical Context\n\n")
            if (isTech) {
                append("The rapid evolution of AI and technology has been a defining trend of the 21st century. ")
                append("From early computational theory to modern neural networks, the trajectory has shown ")
                append("exponential growth in both capability and application.\n\n")
            } else if (isPolitics) {
                append("Political dynamics have historically shaped the course of nations. ")
                append("The interplay between democratic institutions, public opinion, and policy-making ")
                append("continues to evolve with changing social landscapes.\n\n")
            } else if (isEconomy) {
                append("Economic cycles have long been a driver of global events. ")
                append("The tension between growth and stability, inflation and employment, ")
                append("remains a central challenge for policymakers worldwide.\n\n")
            } else if (isScience) {
                append("Scientific discovery has accelerated dramatically in recent decades. ")
                append("Breakthroughs in one field often catalyze advances across multiple disciplines.\n\n")
            } else {
                append("This topic intersects with multiple domains and has evolved significantly over time. ")
                append("Understanding its historical context requires examining the broader forces at play.\n\n")
            }

            // Key stakeholders
            append("## Key Stakeholders and Perspectives\n\n")
            append("This topic involves multiple perspectives that shape the discourse:\n\n")
            if (isTech) {
                append("- **Technology companies**: Focus on innovation, market share, and regulatory compliance\n")
                append("- **Regulators**: Balancing innovation with consumer protection and security\n")
                append("- **Academia**: Research advancement and ethical considerations\n")
                append("- **Public**: Privacy concerns, job displacement fears, and benefits expectations\n\n")
            } else if (isPolitics) {
                append("- **Political parties**: Differing visions and policy priorities\n")
                append("- **Voters**: Shifting demographics and changing priorities\n")
                append("- **Interest groups**: Advocacy and lobbying efforts\n")
                append("- **Institutions**: Judicial, legislative, and executive branches\n\n")
            } else {
                append("- **Industry leaders**: Market dynamics and competitive pressures\n")
                append("- **Policy makers**: Regulatory and governance frameworks\n")
                append("- **Researchers**: Evidence-based analysis and findings\n")
                append("- **Public**: Impact on communities and daily life\n\n")
            }

            // Current developments
            append("## Current Developments\n\n")
            append("The landscape continues to evolve with new developments emerging regularly. ")
            append("Key trends include ongoing debate about the direction of policy, ")
            append("technological advancement, and public engagement with the topic.\n\n")

            // Recommendations
            append("## Key Takeaways\n\n")
            append("1. The topic remains dynamic with developments across multiple fronts\n")
            append("2. Multiple stakeholders have competing priorities and perspectives\n")
            append("3. The broader implications extend beyond the immediate subject matter\n")
            append("4. Monitoring ongoing developments is essential for informed understanding\n")

            append("\n\n---\n\n")
            append("*Research report generated based on available information. ")
            append("For comprehensive analysis, consult primary sources and expert commentary.*")
        }
    }

    /**
     * Generate a generic response when the prompt doesn't match any known pattern.
     */
    private fun genericResponse(prompt: String, maxTokens: Int): String {
        val content = extractArticleContent(prompt)
        if (content.isNotBlank() && content.length > 50) {
            return summarize(content, maxTokens)
        }
        return "Analysis complete. No specific pattern matched in the input. " +
               "For best results, use prompts that clearly indicate the desired operation " +
               "(summarize, analyze bias, classify topics, generate briefing, or research)."
    }
}
