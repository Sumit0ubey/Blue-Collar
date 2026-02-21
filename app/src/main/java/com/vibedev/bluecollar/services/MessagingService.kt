package com.vibedev.bluecollar.services

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vibedev.bluecollar.MainActivity
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.ui.notification.IncomingJobNotifications
import com.vibedev.bluecollar.utils.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logDebug("FCM", "New token: $token")


        if (!AppData.authToken.isNullOrBlank()) {
            val svc = RealtimeNotificationService(applicationContext)
            CoroutineScope(Dispatchers.IO).launch {
                svc.registerOrUpdatePushTarget(token)
            }
        } else {
            logDebug("FCM", "Not logged in yet. Token saved; register after login.")
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: message.notification?.title ?: "BlueCollar"
        val body = message.data["body"] ?: message.notification?.body ?: "You have a new notification"

        val screen = message.data["screen"]?.trim()?.lowercase() ?: "main"
        val jobId = message.data["jobId"]?.trim() ?: ""
        val notificationType = message.data["notificationType"] ?: ""
        val uriStr = message.data["uri"]?.trim() ?: ""

        val deepLinkIntent = when {
            screen.isNotEmpty() -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("screen", screen)
                    putExtra("jobId", jobId)
                    putExtra("notificationType", notificationType)
                }
            }

            uriStr.isNotEmpty() -> {
                Intent(Intent.ACTION_VIEW, uriStr.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            else -> {
                Intent(this, MainActivity::class.java)
            }
        }

        IncomingJobNotifications.showGeneralPush(
            context = applicationContext,
            title = title,
            body = body,
            deepLinkIntent = deepLinkIntent
        )
        logDebug("FCM", "Push received: data=${message.data}")
    }

}
