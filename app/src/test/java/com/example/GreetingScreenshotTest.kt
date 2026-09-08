package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.LocalDateCompat
import com.example.domain.prediction.CyclePredictionEngine
import com.example.ui.screens.home.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun home_screen_screenshot() {
    val prediction = CyclePredictionEngine.predict(
      pastCycles = emptyList(),
      defaultCycleLength = 28,
      defaultPeriodLength = 5,
      fallbackLastPeriodStart = LocalDateCompat.now().minusDays(14),
      today = LocalDateCompat.now()
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        HomeScreen(
          prediction = prediction,
          todayLog = null,
          userSettings = null,
          allDailyLogs = emptyList(),
          onOpenDayDetail = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
  }
}
