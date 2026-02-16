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

    @SuppressLint("MissingPermission")
    fun showIncomingFullScreen(
        context: Context,
        jobId: String,
        payload: Map<String, Any?>
    ) {
        createChannels(context)

        val intent = Intent(context, IncomingRequestActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("jobId", jobId)
            putExtra("city", payload["city"] as? String ?: "")
            putExtra("serviceType", (payload["serviceType"] as? String) ?: "")
            putExtra("name", payload["name"] as? String ?: "")
            putExtra("number", payload["number"] as? String ?: "")
            putExtra("description", payload["description"] as? String ?: "")
            putExtra("address", payload["address"] as? String ?: "")
            putExtra("cost", payload["cost"] as? String ?: "")
        }

        val pi = PendingIntent.getActivity(
            context,
            jobId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notif = NotificationCompat.Builder(context, INCOMING_REQUEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.notifications_icon)
            .setContentTitle("Service Request")
            .setContentText("Tap to respond")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pi, true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(jobId.hashCode(), notif)

        Handler(Looper.getMainLooper()).postDelayed({
            NotificationManagerCompat.from(context).cancel(jobId.hashCode())
        }, 10_000)
    }
}
