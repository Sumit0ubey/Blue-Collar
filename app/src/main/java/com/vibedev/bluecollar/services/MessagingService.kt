package com.vibedev.bluecollar.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.utils.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        logDebug("FCM", "Push received: data=${message.data}")
    }

}
