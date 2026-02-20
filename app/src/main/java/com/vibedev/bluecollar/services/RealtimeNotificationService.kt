package com.vibedev.bluecollar.services

import android.content.Context
import android.provider.Settings
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.utils.logDebug
import com.vibedev.bluecollar.utils.logError
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account

class RealtimeNotificationService(private val context: Context) {

    private val client = AppwriteManager.getClient()
    private val account = Account(client)

    suspend fun registerOrUpdatePushTarget(fcmToken: String) {

        val targetId = stableTargetId()

        try {
            account.createPushTarget(
                targetId = targetId,
                identifier = fcmToken,
                providerId = AppData.MESSAGING_PROVIDER_ID
            )

            logDebug("PushTarget", "Created push target")

        } catch (e: AppwriteException) {

            if (e.code == 409) {
                try {
                    account.updatePushTarget(
                        targetId = targetId,
                        identifier = fcmToken
                    )

                    logDebug("PushTarget", "Updated push target")

                } catch (ex: Exception) {
                    logError("PushTarget", "Update failed", ex)
                }
            } else {
                logError("PushTarget", "Create failed", e)
            }
        }
    }


    private fun stableTargetId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return "android_$androidId"
    }
}
