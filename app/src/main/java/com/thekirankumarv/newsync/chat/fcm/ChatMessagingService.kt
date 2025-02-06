package com.thekirankumarv.newsync.chat.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.thekirankumarv.newsync.chat.presentation.utils.AuthUtils

class ChatMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val firestore = FirebaseFirestore.getInstance()
        val userId = AuthUtils.getCurrentUserId() // Using our new utility function
        if (userId != null) {
            firestore.collection("users").document(userId)
                .update("fcmToken", token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Show notification
        message.notification?.let {
            showNotification(it.title ?: "New Message", it.body ?: "")
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "chat_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chat Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
