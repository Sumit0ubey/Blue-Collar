package com.vibedev.bluecollar.services

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.Notification
import com.vibedev.bluecollar.utils.formatTimeAgo
import com.vibedev.bluecollar.utils.logError
import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.services.Databases

class NotificationService(client: Client) {

    private val databases = Databases(client)
    private val TAG = "NotificationService"

    suspend fun getNotifications(): List<Notification>? {
        val userId = AppData.authToken
        if (userId.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            val response = databases.listDocuments(
                AppData.DATABASE_ID,
                AppData.NOTIFICATION_COLLECTION_ID,
                listOf(Query.equal("customerID", userId), Query.orderDesc($$"$updatedAt"))
            )

            response.documents.map { doc ->
                val data = doc.data
                Notification(
                    id = doc.id,
                    title = data["title"] as String,
                    message = data["description"] as String,
                    time = formatTimeAgo(data[$$"$updatedAt"] as String),
                    iconRes = data["iconResource"] as String,
                    isRead = data["isRead"] as Boolean,
                )
            }
        } catch (e: Exception) {
            logError(TAG, "Error getting notification of user $userId", e)
            null
        }
    }
}