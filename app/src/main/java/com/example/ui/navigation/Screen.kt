package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Main : Screen("main")
    object DayDetail : Screen("day_detail/{date}") {
        fun createRoute(date: String) = "day_detail/$date"
    }
}

enum class MainTab(val label: String, val route: String) {
    HOME("Today", "tab_home"),
    CALENDAR("Calendar", "tab_calendar"),
    STATS("Insights", "tab_stats"),
    SETTINGS("Settings", "tab_settings")
}
