package com.rooknews.app.navigation

/**
 * 5-tab navigation for RookNews.
 */
enum class Tab(
    val route: String,
    val title: String,
    val iconSelected: String,
    val iconUnselected: String
) {
    HOME(
        route = "home",
        title = "Home",
        iconSelected = "News",
        iconUnselected = "OutlineNewspaper"
    ),
    FEEDS(
        route = "feeds",
        title = "Feeds",
        iconSelected = "RssFeed",
        iconUnselected = "RssFeed"
    ),
    BRIEFINGS(
        route = "briefings",
        title = "Briefings",
        iconSelected = "AutoAwesome",
        iconUnselected = "AutoAwesome"
    ),
    RESEARCH(
        route = "research",
        title = "Research",
        iconSelected = "Search",
        iconUnselected = "Search"
    ),
    SETTINGS(
        route = "settings",
        title = "Settings",
        iconSelected = "Settings",
        iconUnselected = "Settings"
    )
}
