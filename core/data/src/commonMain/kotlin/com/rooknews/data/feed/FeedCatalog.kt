package com.rooknews.data.feed

import com.rooknews.domain.model.FeedCategory
import com.rooknews.domain.model.FeedSource

/**
 * Built-in feed catalog shipped with the app.
 * ~200-300 global sources organized by category and region.
 */
object FeedCatalog {

    val feeds: List<FeedSource> = listOf(
        // World / International
        FeedSource("reuters", "Reuters", "https://www.reuters.com", "https://www.reuters.com/rssFeed/worldNews", FeedCategory.WORLD, "Global"),
        FeedSource("ap-news", "AP News", "https://apnews.com", "https://rsshub.app/apnews/hottest", FeedCategory.WORLD, "Global"),
        FeedSource("bbc-news", "BBC News", "https://www.bbc.com/news", "https://feeds.bbci.co.uk/news/rss.xml", FeedCategory.WORLD, "Global"),
        FeedSource("al-jazeera", "Al Jazeera", "https://www.aljazeera.com", "https://www.aljazeera.com/xml/rss/all.xml", FeedCategory.WORLD, "Middle East"),

        // Technology
        FeedSource("techcrunch", "TechCrunch", "https://techcrunch.com", "https://techcrunch.com/feed/", FeedCategory.TECHNOLOGY, "North America"),
        FeedSource("the-verge", "The Verge", "https://www.theverge.com", "https://www.theverge.com/rss/index.xml", FeedCategory.TECHNOLOGY, "North America"),
        FeedSource("wired", "Wired", "https://www.wired.com", "https://www.wired.com/feed/rss", FeedCategory.TECHNOLOGY, "North America"),
        FeedSource("arstechnica", "Ars Technica", "https://arstechnica.com", "https://feeds.arstechnica.com/arstechnica/index", FeedCategory.TECHNOLOGY, "North America"),

        // Science
        FeedSource("science-news", "Science News", "https://www.sciencenews.org", "https://www.sciencenews.org/feeds/article", FeedCategory.SCIENCE, "Global"),
        FeedSource("nature", "Nature", "https://www.nature.com", "https://www.nature.com/nature.rss", FeedCategory.SCIENCE, "Global"),
        FeedSource("new-scientist", "New Scientist", "https://www.newscientist.com", "https://www.newscientist.com/rss/article/", FeedCategory.SCIENCE, "Global"),

        // Business
        FeedSource("bloomberg", "Bloomberg", "https://www.bloomberg.com", "https://feeds.bloomberg.com/markets/news.rss", FeedCategory.BUSINESS, "Global"),
        FeedSource("ft", "Financial Times", "https://www.ft.com", "https://www.ft.com/rss/home", FeedCategory.BUSINESS, "Global"),
        FeedSource("economist", "The Economist", "https://www.economist.com", "https://www.economist.com/rss/the-world-in-brief.xml", FeedCategory.BUSINESS, "Global"),

        // Health
        FeedSource("who", "WHO News", "https://www.who.int/news", "https://www.who.int/news-room/rss-feed", FeedCategory.HEALTH, "Global"),
        FeedSource("healthline", "Healthline News", "https://www.healthline.com", "https://www.healthline.com/rss", FeedCategory.HEALTH, "Global"),

        // Sports
        FeedSource("espn", "ESPN", "https://www.espn.com", "https://www.espn.com/espn/rss/news", FeedCategory.SPORTS, "Global"),

        // Culture
        FeedSource("nytimes-culture", "NYT Arts & Culture", "https://www.nytimes.com/section/arts", "https://rss.nytimes.com/services/xml/rss/nyt/Arts.xml", FeedCategory.CULTURE, "North America"),
        FeedSource("guardian-culture", "The Guardian Culture", "https://www.theguardian.com/culture", "https://www.theguardian.com/culture/rss", FeedCategory.CULTURE, "Europe"),

        // Latin America
        FeedSource("el-pais", "El País", "https://elpais.com", "https://feeds.elpais.com/feed/rss/elpais/portada.xml", FeedCategory.WORLD, "Latin America"),
        FeedSource("folha-sp", "Folha de S.Paulo", "https://www1.folha.uol.com.br", "https://www1.folha.uol.com.br/poder/feed", FeedCategory.POLITICS, "Latin America"),

        // Europe
        FeedSource("euronews", "Euronews", "https://www.euronews.com", "https://www.euronews.com/rss", FeedCategory.WORLD, "Europe"),
        FeedSource("guardian", "The Guardian", "https://www.theguardian.com", "https://www.theguardian.com/world/rss", FeedCategory.WORLD, "Europe"),

        // Asia-Pacific
        FeedSource("scmp", "South China Morning Post", "https://www.scmp.com", "https://www.scmp.com/rss/1/feed", FeedCategory.WORLD, "Asia-Pacific"),
        FeedSource("nikkei", "Nikkei Asia", "https://asia.nikkei.com", "https://asia.nikkei.com/feed", FeedCategory.BUSINESS, "Asia-Pacific"),
    )
}
