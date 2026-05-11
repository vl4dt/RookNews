package com.rooknews

import android.app.Application
import android.content.Context
import com.rooknews.app.AppKoinModule
import com.rooknews.data.database.ArticleDatabase
import com.rooknews.data.database.DatabaseProvider
import com.rooknews.data.database.setApplicationContext
import com.rooknews.data.feed.FeedCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RookNewsApplication : Application(), KoinComponent {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val articleDatabase: ArticleDatabase by inject()

    override fun onCreate() {
        super.onCreate()

        // Initialize SQLDelight database
        DatabaseProvider.setApplicationContext(this)

        // Read API key from Android SharedPreferences
        // If not set, Koin will use default and fall back to local inference
        try {
            val prefs = getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            val apiKey = prefs.getString("cloud_api_key", "") ?: ""
            if (apiKey.isNotBlank()) {
                AppKoinModule.setApiKey(apiKey)
            }
        } catch (_: Exception) {
            // Preferences not ready yet — Koin will use default
        }

        startKoin {
            androidLogger()
            androidContext(this@RookNewsApplication)
            modules(AppKoinModule.koinModules())
        }

        // Seed built-in feeds into the database
        scope.launch {
            seedFeeds()
        }
    }

    /**
     * Seeds the built-in feed catalog into the database.
     * Uses INSERT OR REPLACE so it's safe to call multiple times.
     */
    private suspend fun seedFeeds() {
        try {
            val queries = articleDatabase.feedSourceQueries
            FeedCatalog.feeds.forEach { feed ->
                queries.insertFeed(
                    id = feed.id,
                    title = feed.title,
                    url = feed.url,
                    feedUrl = feed.feedUrl,
                    category = feed.category.name,
                    region = feed.region,
                    language = feed.language,
                    enabled = if (feed.enabled) 1L else 0L,
                    lastFetched = feed.lastFetched
                )
            }
        } catch (_: Exception) {
            // Seed failure is non-critical — feeds will be seeded on first Feeds tab load
        }
    }
}
