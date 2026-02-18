package com.vibedev.bluecollar.services

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.UserProfile
import com.vibedev.bluecollar.utils.logError
import io.appwrite.Client
import io.appwrite.Permission
import io.appwrite.Role
import io.appwrite.services.Databases

class ProfileService(client: Client) {

    private val databases = Databases(client)
    private val TAG = "ProfileService"

    suspend fun createProfile(userId: String, userName: String, userNumber: String, userCity: String, userAddress: String, isServiceProvider: Boolean, serviceType: String, profileImage: String) {
        try {
            val permissions = listOf(
                Permission.read(Role.user(userId)),
                Permission.update(Role.user(userId)),
                Permission.delete(Role.user(userId))
            )
            databases.createDocument(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.PROFILE_COLLECTION_ID,
                documentId = userId,
                data = mapOf(
                    "name" to userName,
                    "phone" to userNumber,
                    "city" to userCity,
                    "address" to userAddress,
                    "isServiceProvider" to isServiceProvider,
                    "serviceType" to serviceType,
                    "profilePicUrl" to profileImage,
                ),
                permissions = permissions
            )
        } catch (e: Exception) {
            logError(TAG, "Error creating profile for user $userId", e)
        }
    }

    suspend fun doesProfileExist(userId: String): Boolean {
        return try {
            databases.getDocument(
                AppData.DATABASE_ID,
                AppData.PROFILE_COLLECTION_ID,
                userId
            )
            true
        } catch (e: Exception) {
            logError(TAG, "Error checking if profile exists for user $userId", e)
            false
        }
    }


    suspend fun getProfile(userId: String): UserProfile? {
        return try {
            val doc = databases.getDocument(
                AppData.DATABASE_ID,
                AppData.PROFILE_COLLECTION_ID,
                userId
            )

            val data = doc.data
            UserProfile(
                name = data["name"] as? String,
                phone = data["phone"] as? String,
                city = data["city"] as? String,
                address = data["address"] as? String,
                isServiceProvider = data["isServiceProvider"] as? Boolean ?: false,
                serviceType = data["serviceType"] as? String,
                rating = data["avgRating"] as? String,
                profileImage = data["profilePicUrl"] as? String,
                about = data["about"] as? String,
                portfolio = data["portfolioPic"] as? List<String>
            )
        } catch (e: Exception) {
            logError(TAG, "Error getting profile for user $userId", e)
            null
        }
    }

    suspend fun updateProfile(userId: String, data: Map<String, Any?>) {
        try {
            databases.updateDocument(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.PROFILE_COLLECTION_ID,
                documentId = userId,
                data = data
            )
        } catch (e: Exception) {
            logError(TAG, "Error updating profile for user $userId", e)
        }
    }
}
