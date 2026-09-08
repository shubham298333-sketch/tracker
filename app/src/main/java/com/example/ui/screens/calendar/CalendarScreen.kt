package com.example.ui.screens.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.domain.model.LocalDateCompat
import com.example.domain.prediction.CyclePredictionResult
import com.example.ui.theme.ColorFertileWindow
import com.example.ui.theme.ColorOvulation
import com.example.ui.theme.ColorPeriodPredicted
import com.example.ui.theme.ColorPeriodSolid
import com.example.ui.theme.ColorSymptomDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    prediction: CyclePredictionResult?,
    allDailyLogs: List<DailyLog>,
    onSelectDate: (String) -> Unit
) {
    val today = remember { LocalDateCompat.now() }
    var currentYear by remember { mutableIntStateOf(today.year) }
    var currentMonth by remember { mutableIntStateOf(today.month) }
    var showLegendSheet by remember { mutableStateOf(false) }

    val logsMap = remember(allDailyLogs) {
        allDailyLogs.associateBy { it.date }
    }

    val daysInMonth = remember(currentYear, currentMonth) {
        LocalDateCompat.daysInMonth(currentYear, currentMonth)
    }

    val firstDayOfMonth = remember(currentYear, currentMonth) {
        LocalDateCompat(currentYear, currentMonth, 1)
    }
    // Monday = 1, Sunday = 7
    val startDayOfWeek = remember(firstDayOfMonth) {
        firstDayOfMonth.dayOfWeek()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val monthNames = arrayOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            Column {
                Text(
                    text = "${monthNames[currentMonth - 1]} $currentYear",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Tap any date to log symptoms or flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (currentMonth == 1) {
                            currentMonth = 12
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }

                IconButton(
                    onClick = {
                        if (currentMonth == 12) {
                            currentMonth = 1
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }

                IconButton(onClick = { showLegendSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View Legend",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday Row (Mon - Sun)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
            weekdays.forEach { dayLabel ->
                Text(
                    text = dayLabel,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Month Calendar Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                val totalSlots = 42 // 6 weeks of 7 days
                val totalWeeks = if (startDayOfWeek - 1 + daysInMonth > 35) 6 else 5

                for (weekIndex in 0 until totalWeeks) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayOfWeekIndex in 0 until 7) {
                            val slotIndex = weekIndex * 7 + dayOfWeekIndex
                            val dayNumber = slotIndex - (startDayOfWeek - 1) + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = LocalDateCompat(currentYear, currentMonth, dayNumber)
                                val dateIso = date.toIsoString()
                                val log = logsMap[dateIso]

                                val isToday = date == today
                                val isLoggedPeriod = log?.isPeriodDay == true
                                val isPredictedPeriod = !isLoggedPeriod && (prediction?.predictedPeriodDays?.contains(date) == true)
                                val isOvulation = prediction?.ovulationDay == date
                                val isFertile = !isOvulation && (prediction?.fertileWindowDays?.contains(date) == true)
                                val hasSymptoms = (log?.symptoms?.isNotEmpty() == true) || (log?.moods?.isNotEmpty() == true)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isLoggedPeriod -> ColorPeriodSolid
                                                isPredictedPeriod -> ColorPeriodPredicted.copy(alpha = 0.35f)
                                                isOvulation -> ColorOvulation.copy(alpha = 0.25f)
                                                isFertile -> ColorFertileWindow.copy(alpha = 0.22f)
                                                isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (isToday) Modifier.border(
                                                1.5.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(12.dp)
                                            )
                                            else if (isPredictedPeriod) Modifier.border(
                                                1.dp,
                                                ColorPeriodPredicted,
                                                RoundedCornerShape(12.dp)
                                            )
                                            else Modifier
                                        )
                                        .clickable { onSelectDate(dateIso) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isToday || isLoggedPeriod) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isLoggedPeriod -> Color.White
                                                isOvulation -> ColorOvulation
                                                isFertile -> ColorFertileWindow
                                                isToday -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        // Marker / Dot
                                        if (isOvulation) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Ovulation day",
                                                modifier = Modifier.size(10.dp),
                                                tint = ColorOvulation
                                            )
                                        } else if (hasSymptoms) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isLoggedPeriod) Color.White
                                                        else ColorSymptomDot
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Empty slot
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // In-line Legend Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Calendar Legend",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = ColorPeriodSolid, label = "Logged Period")
                    LegendItem(color = ColorPeriodPredicted, label = "Predicted Period", isOutline = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = ColorFertileWindow, label = "Fertile Window")
                    LegendItem(color = ColorOvulation, label = "Ovulation Day", hasStar = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem(color = ColorSymptomDot, label = "Symptoms Logged", isDot = true)
                    LegendItem(color = MaterialTheme.colorScheme.primary, label = "Today", isRing = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Bottom Sheet with Detailed Legend and Fertility Explanation
    if (showLegendSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showLegendSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Cycle Symbols & Legend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                LegendRow(
                    color = ColorPeriodSolid,
                    title = "Logged Period (Solid Rose)",
                    desc = "Days you actively entered menstrual bleeding or spotting."
                )
                LegendRow(
                    color = ColorPeriodPredicted,
                    title = "Predicted Period (Lighter Pink)",
                    desc = "Forecasted period days calculated from your cycle history."
                )
                LegendRow(
                    color = ColorFertileWindow,
                    title = "Fertile Window (Teal)",
                    desc = "The ~6 days when intercourse is most likely to result in pregnancy."
                )
                LegendRow(
                    color = ColorOvulation,
                    title = "Ovulation Day (Star)",
                    desc = "Estimated egg release day, typically 14 days before your next period."
                )
                LegendRow(
                    color = ColorSymptomDot,
                    title = "Small Dot",
                    desc = "Indicates logged symptoms, moods, BBT, or personal notes."
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isOutline: Boolean = false,
    hasStar: Boolean = false,
    isDot: Boolean = false,
    isRing: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isOutline || isRing) color.copy(alpha = 0.2f) else color)
                .then(
                    if (isOutline || isRing) Modifier.border(1.5.dp, color, RoundedCornerShape(4.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasStar) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = Color.White
                )
            } else if (isDot) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LegendRow(color: Color, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
