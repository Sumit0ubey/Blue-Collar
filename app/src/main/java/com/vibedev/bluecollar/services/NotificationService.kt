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
    private val tag = "NotificationService"

    suspend fun getNotifications(): List<Notification>? {
        val userId = AppData.authToken
        if (userId.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            val response = databases.listDocuments(
                AppData.DATABASE_ID,
                AppData.NOTIFICATION_COLLECTION_ID,
                listOf(Query.equal("customerID", userId), Query.equal("isShow", true), Query.orderDesc($$"$createdAt"))
            )

            response.documents.map { doc ->
                val data = doc.data
                Notification(
                    id = doc.id,
                    title = data["title"] as String,
                    message = data["description"] as String,
                    time = formatTimeAgo(data[$$"$createdAt"] as String),
                    iconRes = data["iconResource"] as String,
                    isRead = data["isRead"] as Boolean,
                )
            }
        } catch (e: Exception) {
            logError(tag, "Error getting notification of user $userId", e)
            null
        }
    }

    suspend fun updateNotificationToNotShow(notificationId: String, isShow: Boolean = true) {
        try {
            databases.updateDocument(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.NOTIFICATION_COLLECTION_ID,
                documentId = notificationId,
                data = mapOf(
                    "isShow" to isShow,
                )
            )
        }catch (e: Exception){
            logError(tag, "Error updating notification $notificationId", e)
        }
    }

    suspend fun updateNotificationToRed(notificationId: String, isRead: Boolean = false) {
        try {
            databases.updateDocument(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.NOTIFICATION_COLLECTION_ID,
                documentId = notificationId,
                data = mapOf(
                    "isRead" to isRead,
                )
            )
        }catch (e: Exception){
            logError(tag, "Error updating notification $notificationId", e)
        }
    }
}