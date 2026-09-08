package com.example.ui.screens.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyLog
import com.example.domain.model.LocalDateCompat
import com.example.ui.theme.ColorPeriodSolid
import com.example.ui.theme.TealSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DayDetailScreen(
    dateString: String,
    existingLog: DailyLog?,
    onSaveLog: (DailyLog) -> Unit,
    onDeleteLog: (String) -> Unit,
    onBack: () -> Unit
) {
    val date = remember(dateString) { LocalDateCompat.parseOrNull(dateString) ?: LocalDateCompat.now() }

    var isPeriodDay by remember { mutableStateOf(existingLog?.isPeriodDay ?: false) }
    var flowIntensity by remember { mutableStateOf(existingLog?.flowIntensity ?: DailyLog.FLOW_MEDIUM) }
    var selectedSymptoms by remember { mutableStateOf(existingLog?.symptoms?.toSet() ?: emptySet()) }
    var customSymptomInput by remember { mutableStateOf("") }
    var availableSymptoms by remember {
        mutableStateOf(DailyLog.DEFAULT_SYMPTOMS + (existingLog?.symptoms?.filterNot { DailyLog.DEFAULT_SYMPTOMS.contains(it) } ?: emptyList()))
    }
    var selectedMoods by remember { mutableStateOf(existingLog?.moods?.toSet() ?: emptySet()) }
    var selectedDischarge by remember { mutableStateOf(existingLog?.discharge ?: DailyLog.DISCHARGE_NONE) }
    var bbtInput by remember { mutableStateOf(existingLog?.bbt?.toString() ?: "") }
    var selectedSex by remember { mutableStateOf(existingLog?.sexualActivity ?: DailyLog.SEX_NONE) }
    var notesInput by remember { mutableStateOf(existingLog?.notes ?: "") }

    LaunchedEffect(existingLog) {
        if (existingLog != null) {
            isPeriodDay = existingLog.isPeriodDay
            flowIntensity = existingLog.flowIntensity
            selectedSymptoms = existingLog.symptoms.toSet()
            selectedMoods = existingLog.moods.toSet()
            selectedDischarge = existingLog.discharge
            bbtInput = existingLog.bbt?.toString() ?: ""
            selectedSex = existingLog.sexualActivity
            notesInput = existingLog.notes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = date.formatDisplay(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (date == LocalDateCompat.now()) "Today" else "Log Entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("day_detail_back_button")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (existingLog != null) {
                    IconButton(
                        onClick = {
                            onDeleteLog(dateString)
                            onBack()
                        },
                        modifier = Modifier.testTag("delete_log_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete log",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // 1. Period & Flow Intensity Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPeriodDay) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ColorPeriodSolid.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = ColorPeriodSolid
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Period Bleeding",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPeriodDay) "Bleeding logged for this date" else "No period bleeding",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isPeriodDay,
                            onCheckedChange = { isPeriodDay = it },
                            modifier = Modifier.testTag("period_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ColorPeriodSolid,
                                checkedTrackColor = ColorPeriodSolid.copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (isPeriodDay) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Flow Intensity",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val flows = listOf(
                                DailyLog.FLOW_SPOTTING to "Spotting",
                                DailyLog.FLOW_LIGHT to "Light",
                                DailyLog.FLOW_MEDIUM to "Medium",
                                DailyLog.FLOW_HEAVY to "Heavy"
                            )
                            flows.forEach { (flowKey, flowLabel) ->
                                val isSelected = flowIntensity == flowKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { flowIntensity = flowKey },
                                    label = { Text(flowLabel, fontSize = 13.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ColorPeriodSolid,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Symptoms Section
            Text(
                text = "Symptoms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableSymptoms.forEach { symptom ->
                    val isSelected = selectedSymptoms.contains(symptom)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedSymptoms = if (isSelected) selectedSymptoms - symptom else selectedSymptoms + symptom
                        },
                        label = { Text(symptom) },
                        leadingIcon = if (isSelected) {
                            { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            // Custom symptom input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customSymptomInput,
                    onValueChange = { customSymptomInput = it },
                    placeholder = { Text("Add custom symptom", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val trimmed = customSymptomInput.trim()
                        if (trimmed.isNotEmpty() && !availableSymptoms.contains(trimmed)) {
                            availableSymptoms = availableSymptoms + trimmed
                            selectedSymptoms = selectedSymptoms + trimmed
                            customSymptomInput = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add custom symptom")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Moods Section
            Text(
                text = "Mood & Mental State",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DailyLog.DEFAULT_MOODS.forEach { mood ->
                    val isSelected = selectedMoods.contains(mood)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedMoods = if (isSelected) selectedMoods - mood else selectedMoods + mood
                        },
                        label = { Text(mood) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfied,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Cervical Discharge (Fertility indicator)
            Text(
                text = "Cervical Discharge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dischargeTypes = listOf(
                    DailyLog.DISCHARGE_NONE to "None",
                    DailyLog.DISCHARGE_STICKY to "Sticky",
                    DailyLog.DISCHARGE_CREAMY to "Creamy",
                    DailyLog.DISCHARGE_WATERY to "Watery",
                    DailyLog.DISCHARGE_EGG_WHITE to "Egg-white (Fertile)"
                )
                dischargeTypes.forEach { (typeKey, typeLabel) ->
                    val isSelected = selectedDischarge == typeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDischarge = typeKey },
                        label = { Text(typeLabel) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Basal Body Temperature (BBT) & Sexual Activity
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // BBT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = TealSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Basal Temp (BBT)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        OutlinedTextField(
                            value = bbtInput,
                            onValueChange = { bbtInput = it },
                            placeholder = { Text("e.g. 36.6") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(130.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sexual Activity
                    Text(
                        text = "Sexual Activity",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sexOptions = listOf(
                            DailyLog.SEX_NONE to "None",
                            DailyLog.SEX_PROTECTED to "Protected",
                            DailyLog.SEX_UNPROTECTED to "Unprotected"
                        )
                        sexOptions.forEach { (sexKey, sexLabel) ->
                            val isSelected = selectedSex == sexKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSex = sexKey },
                                label = { Text(sexLabel, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Notes
            Text(
                text = "Notes & Thoughts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                placeholder = { Text("Write personal observations, energy levels, cravings, or health notes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Bottom Sticky Save Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = {
                    val log = DailyLog(
                        date = dateString,
                        isPeriodDay = isPeriodDay,
                        flowIntensity = if (isPeriodDay) flowIntensity else DailyLog.FLOW_NONE,
                        symptoms = selectedSymptoms.toList(),
                        moods = selectedMoods.toList(),
                        discharge = selectedDischarge,
                        bbt = bbtInput.toFloatOrNull(),
                        sexualActivity = selectedSex,
                        notes = notesInput
                    )
                    onSaveLog(log)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_daily_log_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
