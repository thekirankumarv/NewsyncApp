package com.thekirankumarv.newsync.chat.presentation.utils


import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.formatTime(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this

    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    return when {
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
            // Today, show time only
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
        }
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> {
            // Yesterday
            "Yesterday " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(this)
        }
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
            // Same year
            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(this)
        }
        else -> {
            // Different year
            SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(this)
        }
    }
}