package com.example

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ads.AdManager
import com.example.data.model.UserSettings
import com.example.domain.model.LocalDateCompat
import com.example.ui.navigation.Screen
import com.example.ui.screens.daydetail.DayDetailScreen
import com.example.ui.screens.lock.PinLockScreen
import com.example.ui.screens.main.MainScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.resolveIsDarkTheme
import com.example.ui.viewmodel.CycleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.initialize(this)

        setContent {
            val cycleViewModel: CycleViewModel = viewModel()
            val userSettings by cycleViewModel.userSettings.collectAsState()
            var onboardingTheme by remember { mutableStateOf(UserSettings.THEME_LIGHT) }

            val activeTheme = if (userSettings?.isOnboardingCompleted == true) {
                userSettings?.appTheme ?: UserSettings.THEME_LIGHT
            } else {
                onboardingTheme
            }
            val activePalette = userSettings?.accentPalette ?: UserSettings.PALETTE_ROSE
            val isDark = resolveIsDarkTheme(activeTheme)

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            MyApplicationTheme(
                appTheme = activeTheme,
                accentPalette = activePalette
            ) {
                PeriodTrackerApp(
                    viewModel = cycleViewModel,
                    isDarkTheme = isDark,
                    onboardingTheme = onboardingTheme,
                    onOnboardingThemeChange = { onboardingTheme = it },
                    onShowInterstitial = { forceShow, onDismissed ->
                        AdManager.showInterstitialAd(this@MainActivity, forceShow, onDismissed)
                    },
                    onShowRewarded = { onRewardGranted, onDismissed ->
                        AdManager.showRewardedAd(
                            this@MainActivity,
                            onUserEarnedReward = { onRewardGranted() },
                            onAdDismissed = onDismissed
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun PeriodTrackerApp(
    viewModel: CycleViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onboardingTheme: String = UserSettings.THEME_LIGHT,
    onOnboardingThemeChange: (String) -> Unit = {},
    onShowInterstitial: (forceShow: Boolean, onDismissed: () -> Unit) -> Unit = { _, d -> d() },
    onShowRewarded: (onRewardGranted: () -> Unit, onDismissed: () -> Unit) -> Unit = { r, d -> r(); d() }
) {
    val navController = rememberNavController()

    val userSettings by viewModel.userSettings.collectAsState()
    val allCycles by viewModel.allCycles.collectAsState()
    val allDailyLogs by viewModel.allDailyLogs.collectAsState()
    val prediction by viewModel.predictionResult.collectAsState()
    val todayLog by viewModel.todayLog.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()

    // Request notification permission gracefully for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (isAppLocked) {
        PinLockScreen(
            onVerifyPin = { pin -> viewModel.verifyPin(pin) }
        )
    } else {
        // Check if onboarding completed
        val isOnboarded = userSettings?.isOnboardingCompleted == true

        if (!isOnboarded) {
            OnboardingScreen(
                currentTheme = onboardingTheme,
                onThemeChange = onOnboardingThemeChange,
                onFinishOnboarding = { lastPeriodDate, cycleLen, periodLen, goal, birthYear, onBirthControl ->
                    viewModel.completeOnboarding(
                        lastPeriodStartDate = lastPeriodDate,
                        avgCycleLength = cycleLen,
                        avgPeriodLength = periodLen,
                        goal = goal,
                        birthYear = birthYear,
                        onBirthControl = onBirthControl,
                        appTheme = onboardingTheme
                    )
                }
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = Screen.Main.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Main.route) {
                    MainScreen(
                        prediction = prediction,
                        todayLog = todayLog,
                        userSettings = userSettings,
                        allDailyLogs = allDailyLogs,
                        allCycles = allCycles,
                        isDarkTheme = isDarkTheme,
                        onToggleDayNight = {
                            viewModel.toggleDayNightTheme(isDarkTheme)
                        },
                        onOpenDayDetail = { dateString ->
                            navController.navigate(Screen.DayDetail.createRoute(dateString))
                        },
                        onUpdateSettings = { newSettings ->
                            viewModel.updateSettings(newSettings)
                        },
                        onExportCsv = { onResult ->
                            viewModel.exportDataCsv(onResult)
                        },
                        onClearAllData = {
                            viewModel.clearAllData()
                        },
                        onTestNotification = {
                            viewModel.triggerTestNotification()
                        },
                        onShowInterstitialAd = onShowInterstitial,
                        onShowRewardedAd = onShowRewarded,
                        onUnlockForecast = {
                            viewModel.unlockExtendedForecast()
                        },
                        onUnlockInsights = {
                            viewModel.unlockSymptomInsights()
                        },
                        onGrantAdFreePass = { hours ->
                            viewModel.grantAdFreePass(hours)
                        }
                    )
                }

                composable(
                    route = Screen.DayDetail.route,
                    arguments = listOf(navArgument("date") { type = NavType.StringType })
                ) { backStackEntry ->
                    val dateArg = backStackEntry.arguments?.getString("date") ?: LocalDateCompat.now().toIsoString()
                    val existingLog = allDailyLogs.firstOrNull { it.date == dateArg }

                    DayDetailScreen(
                        dateString = dateArg,
                        existingLog = existingLog,
                        onSaveLog = { log ->
                            viewModel.saveDailyLog(log)
                        },
                        onDeleteLog = { date ->
                            viewModel.deleteDailyLog(date)
                        },
                        onShowInterstitial = { onDismissed ->
                            onShowInterstitial(false, onDismissed)
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
