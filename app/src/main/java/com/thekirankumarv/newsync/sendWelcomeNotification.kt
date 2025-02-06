package com.thekirankumarv.newsync

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

fun sendWelcomeNotification(context: Context, userName: String) {
    try {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if notifications are enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val areNotificationsEnabled = notificationManager.areNotificationsEnabled()
            Log.d("Notification", "Notifications enabled: $areNotificationsEnabled")
        }

        // Check if channel exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel("welcome_channel")
            Log.d("Notification", "Channel exists: ${channel != null}")
        }

        val notification = NotificationCompat.Builder(context, "welcome_channel")
            .setContentTitle("Newsync")
            .setContentText("Welcome $userName")
            .setSmallIcon(R.drawable.icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(1, notification)
        Log.d("Notification", "Notification sent to user: $userName")
    } catch (e: Exception) {
        Log.e("Notification", "Error sending notification", e)
    }
}