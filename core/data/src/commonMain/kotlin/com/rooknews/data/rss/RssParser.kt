package com.rooknews.data.rss

import com.rooknews.domain.model.Article

/**
 * Parses RSS 2.0 and Atom 1.0 XML into a list of [Article] domain objects.
 *
 * Uses a simple string-based parser to avoid pulling in a heavy XML library.
 * Handles the most common RSS/Atom structures found in news feeds.
 */
class RssParser {

    /**
     * Parse RSS/Atom XML string into a list of Articles.
     */
    fun parse(feedXml: String, feedSourceId: String): List<Article> {
        val articles = mutableListOf<Article>()

        val isAtom = feedXml.contains("<feed ", ignoreCase = true) ||
                     feedXml.contains("xmlns=\"http://www.w3.org/2005/Atom\"", ignoreCase = true)

        if (isAtom) {
            parseAtom(feedXml, feedSourceId, articles)
        } else {
            parseRss20(feedXml, feedSourceId, articles)
        }

        return articles
    }

    // -- RSS 2.0 --

    private fun parseRss20(xml: String, feedSourceId: String, out: MutableList<Article>) {
        val items = extractBlocks(xml, "<item", "</item>")

        for (item in items) {
            val title = extractTagContent(item, "title").orEmpty().trim()
            val link = extractTagContent(item, "link")
                      ?: extractTagAttribute(item, "link", "href")
            val guid = extractTagContent(item, "guid")
                      ?: extractTagContent(item, "id")
            val description = extractTagContent(item, "description")
            val content = extractTagContent(item, "content:encoded")
                         ?: extractTagContent(item, "content")
            val author = extractTagContent(item, "dc:creator")
                        ?: extractTagContent(item, "author")
            val pubDateStr = extractTagContent(item, "pubDate")
                            ?: extractTagContent(item, "dc:date")
            val enclosureUrl = extractTagAttribute(item, "enclosure", "url")

            if (title.isEmpty()) continue

            val articleId = guid ?: generateId(feedSourceId, title)

            // Use content:encoded or content for full body; fall back to description
            val fullBody = content?.let { stripHtml(it) }?.takeIf { it.isNotBlank() }
                ?: description?.let { stripHtml(it) }.orEmpty()

            // Summary is a shorter excerpt from description (or body if description is too long)
            val summaryText = description?.let { stripHtml(it) }.orEmpty()
            val summary = if (summaryText.length > 500) summaryText.take(500) else summaryText.takeIf { it.isNotBlank() }

            out.add(
                Article(
                    id = sanitizeId(articleId),
                    feedSourceId = feedSourceId,
                    title = stripHtml(title),
                    summary = summary,
                    bodyMarkdown = fullBody.takeIf { it.isNotBlank() },
                    author = stripHtml(author),
                    publishedAt = parseDate(pubDateStr),
                    fetchedAt = System.currentTimeMillis(),
                    imageUrl = enclosureUrl,
                    originalUrl = link.orEmpty()
                )
            )
        }
    }

    // -- Atom 1.0 --

    private fun parseAtom(xml: String, feedSourceId: String, out: MutableList<Article>) {
        val entries = extractBlocks(xml, "<entry", "</entry>")

        for (entry in entries) {
            val title = extractTagContent(entry, "title").orEmpty().trim()
            val summary = extractTagContent(entry, "summary")
            val content = extractTagContent(entry, "content")
            val author = extractTagContent(entry, "name")
            val updatedStr = extractTagContent(entry, "updated")
                           ?: extractTagContent(entry, "published")
            val id = extractTagContent(entry, "id")
            val link = extractAtomLink(entry)

            if (title.isEmpty()) continue

            val articleId = id ?: generateId(feedSourceId, title)

            // Use content for full body; fall back to summary (Atom's summary tag)
            val fullBody = content?.let { stripHtml(it) }?.takeIf { it.isNotBlank() }
                ?: summary?.let { stripHtml(it) }.orEmpty()

            val summaryText = summary?.let { stripHtml(it) }.orEmpty()
            val atomSummary = if (summaryText.length > 500) summaryText.take(500) else summaryText.takeIf { it.isNotBlank() }

            out.add(
                Article(
                    id = sanitizeId(articleId),
                    feedSourceId = feedSourceId,
                    title = stripHtml(title),
                    summary = atomSummary,
                    bodyMarkdown = fullBody.takeIf { it.isNotBlank() },
                    author = stripHtml(author),
                    publishedAt = parseDate(updatedStr),
                    fetchedAt = System.currentTimeMillis(),
                    originalUrl = link.orEmpty()
                )
            )
        }
    }

    // -- Parsing helpers --

    /**
     * Extract content between <tag> and </tag>.
     */
    private fun extractTagContent(xml: String, tag: String): String? {
        val open = "<${tag}"
        val close = "</${tag}>"

        val openIdx = xml.indexOf(open, ignoreCase = true)
        if (openIdx == -1) return null

        val contentStart = xml.indexOf('>', openIdx)
        if (contentStart == -1) return null

        val closeIdx = xml.indexOf(close, contentStart, ignoreCase = true)
        if (closeIdx == -1) return null

        return xml.substring(contentStart + 1, closeIdx).trim()
    }

    /**
     * Extract an attribute value from a tag.
     */
    private fun extractTagAttribute(xml: String, tag: String, attr: String): String? {
        val tagStart = xml.indexOf("<${tag}", ignoreCase = true)
        if (tagStart == -1) return null

        val tagEnd = xml.indexOf('>', tagStart)
        if (tagEnd == -1) return null

        val tagStr = xml.substring(tagStart, tagEnd + 1)
        val pattern = "${attr}=\""
        val attrIdx = tagStr.indexOf(pattern, ignoreCase = true)
        if (attrIdx == -1) return null

        val valStart = attrIdx + pattern.length
        val valEnd = tagStr.indexOf('"', valStart)
        if (valEnd == -1) return null

        return tagStr.substring(valStart, valEnd)
    }

    /**
     * Extract blocks of text between start and end markers.
     */
    private fun extractBlocks(xml: String, startMarker: String, endMarker: String): List<String> {
        val blocks = mutableListOf<String>()
        var index = 0

        while (index < xml.length) {
            val startIdx = xml.indexOf(startMarker, index, ignoreCase = true)
            if (startIdx == -1) break

            val endIdx = xml.indexOf(endMarker, startIdx, ignoreCase = true)
            if (endIdx == -1) break

            blocks.add(xml.substring(startIdx, endIdx + endMarker.length))
            index = endIdx + endMarker.length
        }

        return blocks
    }

    /**
     * Extract the primary href from Atom <link> elements.
     */
    private fun extractAtomLink(entry: String): String? {
        val links = extractBlocks(entry, "<link", "</link>")
        for (link in links) {
            val href = extractTagAttribute(link, "link", "href")
            if (href != null && !href.contains("self", ignoreCase = true)) {
                return href
            }
        }
        // Fallback: self-closing <link href="..." />
        return extractTagAttribute(entry, "link", "href")
    }

    /**
     * Strip HTML tags to get plain text.
     */
    private fun stripHtml(html: String?): String {
        if (html == null) return ""
        return html.replace("<[^>]*>".toRegex(), "").trim()
    }

    /**
     * Parse a date string (RFC 822 or ISO 8601) to epoch milliseconds.
     * Basic parser that handles common formats without platform dependencies.
     */
    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()

        val trimmed = dateStr.trim()

        // Try ISO 8601: 2024-01-15T10:30:00Z
        if (trimmed.contains('T')) {
            return try {
                val year = trimmed.substring(0, 4).toInt()
                val month = trimmed.substring(5, 7).toInt()
                val day = trimmed.substring(8, 10).toInt()
                approximateEpoch(year, month, day)
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }

        // Try RFC 822: Mon, 15 Jan 2024 10:30:00 +0000
        // or: Mon, 15 Jan 2024
        val monthNames = mapOf(
            "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
            "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
            "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
        )

        return try {
            val parts = trimmed.split(Regex("\\s+"))
            var month = 1
            var day = 1
            var year = 2024

            for (part in parts) {
                when {
                    part.toIntOrNull() != null -> {
                        val num = part.toInt()
                        if (num > 31) year = num
                        else if (num > 12) day = num
                        else if (day == 1) day = num // ambiguous, assume day first
                    }
                    monthNames[part.lowercase()] != null -> {
                        month = monthNames[part.lowercase()]!!
                    }
                }
            }
            approximateEpoch(year, month, day)
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Rough epoch approximation from year/month/day.
     * Good enough for sorting — doesn't need millisecond precision.
     */
    private fun approximateEpoch(year: Int, month: Int, day: Int): Long {
        // Days from 1970 to given date (rough)
        val daysFrom1970 = (year - 1970) * 365 + (year - 1969) / 4 +
                           (month - 1) * 30 + day
        return daysFrom1970 * 24L * 60 * 60 * 1000
    }

    /**
     * Generate a deterministic ID from feed + title.
     */
    private fun generateId(feedSourceId: String, title: String): String {
        val clean = title.replace(Regex("[^a-zA-Z0-9]"), "_").take(100)
        return "${feedSourceId}_${clean}"
    }

    /**
     * Sanitize an ID for database storage.
     */
    private fun sanitizeId(id: String): String {
        return id.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(200)
    }
}
