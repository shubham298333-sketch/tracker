package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Period Tracker"
        val message = intent.getStringExtra(NotificationHelper.EXTRA_MESSAGE) ?: "Cycle notification"
        NotificationHelper.showNotification(context, title, message)
    }
}
