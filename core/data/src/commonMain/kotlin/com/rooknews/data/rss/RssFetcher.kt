package com.rooknews.data.rss

import com.rooknews.data.network.KtorClientFactory
import com.rooknews.domain.model.FeedSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Fetches RSS/Atom feed XML from a given URL.
 */
class RssFetcher(
    private val httpClient: HttpClient = KtorClientFactory.createConfigured()
) {

    /**
     * Fetch the raw XML content of an RSS feed.
     * Returns null if the request fails.
     */
    suspend fun fetchFeedXml(feed: FeedSource): String? {
        return try {
            val response = httpClient.get(feed.feedUrl) {
                headers {
                    append("Accept", "application/rss+xml, application/xml, text/xml, */*")
                    append("User-Agent", "RookNews/1.0 (News Reader)")
                }
            }

            if (response.status.isSuccess()) {
                response.body<String>()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fetch multiple feeds concurrently.
     * Returns a map of feed URL → XML content (null entries omitted).
     */
    suspend fun fetchMultipleFeeds(feeds: List<FeedSource>): Map<String, String> {
        val results = mutableMapOf<String, String>()

        feeds.forEach { feed ->
            val xml = fetchFeedXml(feed)
            xml?.let { results[feed.feedUrl] = it }
        }

        return results
    }
}
