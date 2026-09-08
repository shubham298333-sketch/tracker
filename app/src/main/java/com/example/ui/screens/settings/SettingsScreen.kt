package com.example.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSettings

@Composable
fun SettingsScreen(
    userSettings: UserSettings?,
    onUpdateSettings: (UserSettings) -> Unit,
    onExportCsv: (onResult: (String) -> Unit) -> Unit,
    onClearAllData: () -> Unit,
    onTestNotification: () -> Unit
) {
    val context = LocalContext.current
    val current = userSettings ?: UserSettings()

    var showPinDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showMedicalDisclaimer by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var cycleLengthOverride by remember(current.avgCycleLength) { mutableIntStateOf(current.avgCycleLength) }
    var periodLengthOverride by remember(current.avgPeriodLength) { mutableIntStateOf(current.avgPeriodLength) }
    var pinInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Preferences, cycle customization, privacy & notifications",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Cycle Calibration
        SettingsSectionHeader(title = "Cycle Calibration", icon = Icons.Default.Timeline)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Manual Cycle Override", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(text = "Lock to manual average instead of auto-calculating", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = current.manualCycleOverride,
                        onCheckedChange = { onUpdateSettings(current.copy(manualCycleOverride = it)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Average Cycle Length: $cycleLengthOverride days", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Slider(
                    value = cycleLengthOverride.toFloat(),
                    onValueChange = {
                        cycleLengthOverride = it.toInt()
                        onUpdateSettings(current.copy(avgCycleLength = it.toInt()))
                    },
                    valueRange = 21f..40f,
                    steps = 18
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Average Period Length: $periodLengthOverride days", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Slider(
                    value = periodLengthOverride.toFloat(),
                    onValueChange = {
                        periodLengthOverride = it.toInt()
                        onUpdateSettings(current.copy(avgPeriodLength = it.toInt()))
                    },
                    valueRange = 2f..10f,
                    steps = 7
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Notifications
        SettingsSectionHeader(title = "Reminders & Notifications", icon = Icons.Default.Notifications)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                NotificationToggleRow(
                    title = "Period starting soon",
                    subtitle = "Notify 2 days before predicted start date",
                    checked = current.notifyPeriodSoon,
                    onChecked = { onUpdateSettings(current.copy(notifyPeriodSoon = it)) }
                )
                NotificationToggleRow(
                    title = "Log period reminder",
                    subtitle = "Notify if period is due but not yet entered",
                    checked = current.notifyPeriodLate,
                    onChecked = { onUpdateSettings(current.copy(notifyPeriodLate = it)) }
                )
                NotificationToggleRow(
                    title = "Fertile window starting",
                    subtitle = "Forecast beginning of high fertility window",
                    checked = current.notifyFertileWindow,
                    onChecked = { onUpdateSettings(current.copy(notifyFertileWindow = it)) }
                )
                NotificationToggleRow(
                    title = "Ovulation day",
                    subtitle = "Alert on the estimated ovulation day",
                    checked = current.notifyOvulation,
                    onChecked = { onUpdateSettings(current.copy(notifyOvulation = it)) }
                )
                NotificationToggleRow(
                    title = "Daily symptom reminder",
                    subtitle = "Gentle evening prompt (8:00 PM) to record symptoms",
                    checked = current.notifyDailyLog,
                    onChecked = { onUpdateSettings(current.copy(notifyDailyLog = it)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onTestNotification,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Send Test Notification Now")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Privacy & App Lock
        SettingsSectionHeader(title = "Privacy & Security", icon = Icons.Default.Security)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "App PIN Lock", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (!current.appLockPin.isNullOrBlank()) "PIN code protection is enabled" else "Require a 4-digit PIN to open",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = !current.appLockPin.isNullOrBlank(),
                        onCheckedChange = { isEnabled ->
                            if (isEnabled) {
                                showPinDialog = true
                            } else {
                                onUpdateSettings(current.copy(appLockPin = null))
                            }
                        }
                    )
                }

                if (!current.appLockPin.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showPinDialog = true }) {
                        Text("Change PIN code")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Data Management & Export
        SettingsSectionHeader(title = "Data Management", icon = Icons.Default.Download)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        onExportCsv { csvContent ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Export Period Tracker Data")
                            context.startActivity(shareIntent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Data (CSV / Share)")
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete All Data & Reset App")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Medical Disclaimer & About
        SettingsSectionHeader(title = "About & Disclaimer", icon = Icons.Default.Info)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Medical Disclaimer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This app is not a medical device and does not provide medical advice. Predictions are statistical estimates based on calendar algorithms. Consult a healthcare professional for clinical advice, fertility treatments, or diagnosis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showAboutDialog = true }) {
                        Text("About & Privacy Policy")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // PIN Setup Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-digit PIN") },
            text = {
                Column {
                    Text("Choose a 4-digit PIN to protect your health entries.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { ch -> ch.isDigit() }) pinInput = it },
                        placeholder = { Text("1234") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onUpdateSettings(current.copy(appLockPin = pinInput))
                            pinInput = ""
                            showPinDialog = false
                        }
                    },
                    enabled = pinInput.length == 4
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete All Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently erase all your cycle history, logged symptoms, notes, and preferences from this device. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Period Tracker & Privacy") },
            text = {
                Column {
                    Text(
                        "Period Tracker is an offline-first menstrual cycle & ovulation tracker.\n\n" +
                        "• Privacy: All data is saved exclusively on your phone in a secure local database. No servers, no trackers, no accounts.\n\n" +
                        "• Prediction Model: Employs standard 14-day luteal phase estimation and statistical cycle averaging.\n\n" +
                        "Version 1.0.0"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
