package com.thekirankumarv.newsync

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp
import android.content.Context
import android.util.Log

@HiltAndroidApp
class NewSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "welcome_channel", // Channel ID
                "Welcome Notifications", // Channel name
                NotificationManager.IMPORTANCE_DEFAULT // Importance level
            ).apply {
                description = "Channel for welcome notifications"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("Notification", "Notification channel created: welcome_channel")
        }
    }
}
