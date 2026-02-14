package com.vibedev.bluecollar.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun showToast(context: Context, message: String, isShortDuration: Boolean = true) {
    Toast.makeText(context, message, if (isShortDuration) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}

fun logError(tag: String, message: String, exception: Exception? = null) {
    Log.e(tag, message, exception)
}

fun logError(tag: String, message: String) {
    Log.e(tag, message)
}

fun logDebug(tag: String, message: String) {
    Log.d(tag, message)
}

fun logInfo(tag: String, message: String) {
    Log.i(tag, message)
}

fun logWarning(tag: String, message: String) {
    Log.w(tag, message)
}

fun logVerbose(tag: String, message: String) {
    Log.v(tag, message)
}

fun formatTimeAgo(time: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val date = sdf.parse(time)
    val now = Date()
    val seconds = ((now.time - date.time) / 1000).toInt()
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = days / 365

    return when {
        years > 0 -> if (years == 1) "a year ago" else "$years years ago"
        months > 0 -> if (months == 1) "a month ago" else "$months months ago"
        days > 0 -> if (days == 1) "a day ago" else "$days days ago"
        hours > 0 -> if (hours == 1) "an hour ago" else "$hours hours ago"
        minutes > 0 -> if (minutes == 1) "a minute ago" else "$minutes minutes ago"
        else -> "just now"
    }
}
