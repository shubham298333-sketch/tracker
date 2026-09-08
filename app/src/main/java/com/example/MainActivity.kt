package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.LocalDateCompat
import com.example.ui.navigation.Screen
import com.example.ui.screens.daydetail.DayDetailScreen
import com.example.ui.screens.lock.PinLockScreen
import com.example.ui.screens.main.MainScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CycleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PeriodTrackerApp()
            }
        }
    }
}

@Composable
fun PeriodTrackerApp(viewModel: CycleViewModel = viewModel()) {
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
                onFinishOnboarding = { lastPeriodDate, cycleLen, periodLen, goal, birthYear, onBirthControl ->
                    viewModel.completeOnboarding(
                        lastPeriodStartDate = lastPeriodDate,
                        avgCycleLength = cycleLen,
                        avgPeriodLength = periodLen,
                        goal = goal,
                        birthYear = birthYear,
                        onBirthControl = onBirthControl
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
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
