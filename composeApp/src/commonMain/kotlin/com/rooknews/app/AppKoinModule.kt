package com.rooknews.app

import com.rooknews.ai.aiModule
import com.rooknews.data.dataModule
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Main Koin module configuration.
 *
 * The aiModule requires an API key for cloud inference.
 * By default it uses an empty string — the app will fall back to local inference
 * until the user enters a Gemini API key in Settings.
 *
 * To provide an API key at startup:
 *   startKoin {
 *       modules(AppKoinModule(apiKey = "my-key"))
 *   }
 */
object AppKoinModule {
    private var _apiKey: String? = null

    /**
     * Set the Gemini API key before Koin starts.
     * Called from RookNewsApplication or can be set programmatically
     * after reading user preferences.
     */
    fun setApiKey(key: String) {
        _apiKey = if (key.isBlank()) null else key
    }

    fun getApiKey(): String = _apiKey ?: ""

    fun koinModules(): List<Module> {
        val apiKey = getApiKey()
        return listOf(
            dataModule,
            aiModule(apiKey = apiKey)
        )
    }
}
