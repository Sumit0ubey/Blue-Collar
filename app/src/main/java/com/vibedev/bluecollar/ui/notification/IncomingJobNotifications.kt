package com.vibedev.bluecollar.ui.notification

import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.app.PendingIntent
import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Build

import com.vibedev.bluecollar.R
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.ui.IncomingRequestActivity

object IncomingJobNotifications {
    private const val INCOMING_REQUEST_CHANNEL_ID = "incoming_request"
    private const val ONGOING_SERVICE_CHANNEL_ID = "ongoing_service"

    fun buildOngoingOnlineNotification(context: Context): Notification {
        createChannels(context)
        return NotificationCompat.Builder(context, ONGOING_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications_icon)
            .setContentTitle("BlueCollar")
            .setContentText("You are Online")
            .setOngoing(true)
            .build()
    }

    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val incomingChannel = NotificationChannel(
                INCOMING_REQUEST_CHANNEL_ID,
                "Incoming Request",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            nm.createNotificationChannel(incomingChannel)

            val ongoingChannel = NotificationChannel(
                ONGOING_SERVICE_CHANNEL_ID,
                "Provider Online",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            nm.createNotificationChannel(ongoingChannel)
        }
    }
}
