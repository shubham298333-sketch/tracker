package com.example.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Cycle
import com.example.data.model.DailyLog
import com.example.data.model.UserSettings
import com.example.domain.prediction.CyclePredictionResult
import com.example.ui.navigation.MainTab
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.stats.StatsScreen

@Composable
fun MainScreen(
    prediction: CyclePredictionResult?,
    todayLog: DailyLog?,
    userSettings: UserSettings?,
    allDailyLogs: List<DailyLog>,
    allCycles: List<Cycle>,
    onOpenDayDetail: (String) -> Unit,
    onUpdateSettings: (UserSettings) -> Unit,
    onExportCsv: (onResult: (String) -> Unit) -> Unit,
    onClearAllData: () -> Unit,
    onTestNotification: () -> Unit
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                val tabs = listOf(
                    MainTab.HOME to (Icons.Filled.Home to Icons.Outlined.Home),
                    MainTab.CALENDAR to (Icons.Filled.CalendarMonth to Icons.Outlined.CalendarMonth),
                    MainTab.STATS to (Icons.Filled.Insights to Icons.Outlined.Insights),
                    MainTab.SETTINGS to (Icons.Filled.Settings to Icons.Outlined.Settings)
                )

                tabs.forEach { (tab, icons) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> HomeScreen(
                    prediction = prediction,
                    todayLog = todayLog,
                    userSettings = userSettings,
                    allDailyLogs = allDailyLogs,
                    onOpenDayDetail = onOpenDayDetail
                )
                MainTab.CALENDAR -> CalendarScreen(
                    prediction = prediction,
                    allDailyLogs = allDailyLogs,
                    onSelectDate = onOpenDayDetail
                )
                MainTab.STATS -> StatsScreen(
                    cycles = allCycles,
                    dailyLogs = allDailyLogs,
                    prediction = prediction
                )
                MainTab.SETTINGS -> SettingsScreen(
                    userSettings = userSettings,
                    onUpdateSettings = onUpdateSettings,
                    onExportCsv = onExportCsv,
                    onClearAllData = onClearAllData,
                    onTestNotification = onTestNotification
                )
            }
        }
    }
}
