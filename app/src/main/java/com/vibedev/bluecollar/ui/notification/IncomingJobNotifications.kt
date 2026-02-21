package com.vibedev.bluecollar.ui.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vibedev.bluecollar.MainActivity
import com.vibedev.bluecollar.R

object IncomingJobNotifications {
    private const val INCOMING_REQUEST_CHANNEL_ID = "incoming_request_v2"
    private const val ONGOING_SERVICE_CHANNEL_ID = "ongoing_service"
    private const val GENERAL_PUSH_CHANNEL_ID = "general_push_v2"

    fun buildOngoingOnlineNotification(context: Context): Notification {
        createChannels(context)
        return NotificationCompat.Builder(context, ONGOING_SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("You are currently online")
            .setContentText("You will be notified of new requests.")
            .setOngoing(true)
            .build()
    }

    @SuppressLint("MissingPermission")
    fun showGeneralPush(
        context: Context,
        title: String,
        body: String,
        deepLinkIntent: Intent? = null,
        notificationId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    ) {
        createChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = deepLinkIntent ?: Intent(context, MainActivity::class.java).apply {
            if (component != null) {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, GENERAL_PUSH_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title.ifBlank { "BlueCollar" })
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val incomingChannel = NotificationChannel(
                INCOMING_REQUEST_CHANNEL_ID, "Incoming Requests", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(soundUri, attrs)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            nm.createNotificationChannel(incomingChannel)

            val ongoingChannel = NotificationChannel(
                ONGOING_SERVICE_CHANNEL_ID, "Provider Online", NotificationManager.IMPORTANCE_LOW
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            nm.createNotificationChannel(ongoingChannel)

            val generalPushChannel = NotificationChannel(
                GENERAL_PUSH_CHANNEL_ID, "General Notifications", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(soundUri, attrs)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            nm.createNotificationChannel(generalPushChannel)
        }
    }
}
