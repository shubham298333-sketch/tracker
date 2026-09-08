package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.domain.model.LocalDateCompat
import com.example.domain.prediction.CyclePredictionEngine
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "period_tracker_reminders"
    const val CHANNEL_NAME = "Cycle & Health Reminders"

    const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    const val EXTRA_TITLE = "notification_title"
    const val EXTRA_MESSAGE = "notification_message"

    const val TYPE_PERIOD_SOON = "period_soon"
    const val TYPE_PERIOD_LATE = "period_late"
    const val TYPE_FERTILE_WINDOW = "fertile_window"
    const val TYPE_OVULATION = "ovulation"
    const val TYPE_DAILY_LOG = "daily_log"
    const val TYPE_TEST = "test"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle reminders for periods, ovulation, and daily wellness logs"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String, notificationId: Int = 1001) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    suspend fun scheduleAllAlarms(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val settings = db.userSettingsDao().getSettings() ?: return
        if (!settings.isOnboardingCompleted) return

        val cycles = db.cycleDao().getAllCyclesList()
        val fallbackStart = LocalDateCompat.parseOrNull(settings.lastPeriodStartDate) ?: LocalDateCompat.now()

        val prediction = CyclePredictionEngine.predict(
            pastCycles = cycles,
            defaultCycleLength = settings.avgCycleLength,
            defaultPeriodLength = settings.avgPeriodLength,
            fallbackLastPeriodStart = fallbackStart,
            today = LocalDateCompat.now(),
            userGoal = settings.goal,
            manualCycleOverride = settings.manualCycleOverride
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Period soon reminder
        if (settings.notifyPeriodSoon) {
            val notifyDate = prediction.nextPeriodStart.minusDays(settings.notifyPeriodSoonDays.toLong())
            scheduleAlarmForDate(
                context, alarmManager, notifyDate, 9, 0, 101,
                TYPE_PERIOD_SOON,
                "Period starting soon",
                "Your period is predicted to start in ${settings.notifyPeriodSoonDays} days."
            )
        }

        // 2. Period late / log reminder
        if (settings.notifyPeriodLate) {
            val lateDate = prediction.nextPeriodStart.plusDays(settings.avgPeriodLength.toLong())
            scheduleAlarmForDate(
                context, alarmManager, lateDate, 10, 0, 102,
                TYPE_PERIOD_LATE,
                "Log your period",
                "Your period was expected recently. Don't forget to log it when it arrives!"
            )
        }

        // 3. Fertile window reminder
        if (settings.notifyFertileWindow && prediction.fertileWindowDays.isNotEmpty()) {
            val fertileStart = prediction.fertileWindowDays.first()
            scheduleAlarmForDate(
                context, alarmManager, fertileStart, 9, 0, 103,
                TYPE_FERTILE_WINDOW,
                "Fertile window starting",
                "Your fertile window begins today. Tap to view your cycle forecast."
            )
        }

        // 4. Ovulation day reminder
        if (settings.notifyOvulation) {
            scheduleAlarmForDate(
                context, alarmManager, prediction.ovulationDay, 9, 0, 104,
                TYPE_OVULATION,
                "Ovulation day",
                "Today is your predicted ovulation day."
            )
        }

        // 5. Daily log reminder
        if (settings.notifyDailyLog) {
            val timeParts = settings.dailyLogTime.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 20
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            scheduleDailyAlarm(
                context, alarmManager, hour, minute, 105,
                TYPE_DAILY_LOG,
                "Time to log today's symptoms",
                "Take a moment to record your mood, flow, and symptoms for today."
            )
        }
    }

    private fun scheduleAlarmForDate(
        context: Context,
        alarmManager: AlarmManager,
        date: LocalDateCompat,
        hour: Int,
        minute: Int,
        requestCode: Int,
        type: String,
        title: String,
        message: String
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.month - 1)
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        if (target.after(now)) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_TYPE, type)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
            } catch (e: SecurityException) {
                // If permission not granted, fallback gracefully
            }
        }
    }

    private fun scheduleDailyAlarm(
        context: Context,
        alarmManager: AlarmManager,
        hour: Int,
        minute: Int,
        requestCode: Int,
        type: String,
        title: String,
        message: String
    ) {
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_TYPE, type)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: Exception) {
            // Graceful fallback
        }
    }
}
