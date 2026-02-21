package com.vibedev.bluecollar.services

import android.content.Context
import android.provider.Settings
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.utils.logDebug
import com.vibedev.bluecollar.utils.logError
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import java.security.MessageDigest

class RealtimeNotificationService(private val context: Context) {

    private val client = AppwriteManager.getClient()
    private val account = Account(client)

    suspend fun registerOrUpdatePushTarget(fcmToken: String) {
        if (fcmToken.isBlank()) return

        val myTargetId = stableTargetId()

        try {
            account.createPushTarget(
                targetId = myTargetId,
                identifier = fcmToken,
                providerId = AppData.MESSAGING_PROVIDER_ID
            )
            logDebug("PushTarget", "Created push target: $myTargetId")
        } catch (e: AppwriteException) {
            if (e.code == 409) {
                try {
                    account.updatePushTarget(
                        targetId = myTargetId,
                        identifier = fcmToken
                    )
                    logDebug("PushTarget", "Updated push target: $myTargetId")
                } catch (ex: Exception) {
                    logError("PushTarget", "Update failed", ex)
                    return
                }
            } else {
                logError("PushTarget", "Create failed", e)
                return
            }
        }

        AppwriteManager.functions.pruneOtherTokenForFCM(myTargetId)

    }

     fun stableTargetId(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        val userId = AppData.authToken ?: "anon"

        val digest = sha256("$userId|$androidId").take(20)
        return "and_$digest"
    }

    private fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}