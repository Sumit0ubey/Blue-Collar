package com.vibedev.bluecollar.services

import com.vibedev.bluecollar.utils.logError
import androidx.activity.ComponentActivity

import io.appwrite.ID
import io.appwrite.Client
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.enums.OAuthProvider
import io.appwrite.exceptions.AppwriteException


class AccountService(client: Client) {
    private val account = Account(client)
    private val TAG = "AccountService"

    suspend fun getLoggedIn(): User<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: AppwriteException) {
            logError(TAG, "Error getting logged in user", e)
            null
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            account.createEmailPasswordSession(
                email = email,
                password = password
            )
            true
        } catch (e: AppwriteException) {
            logError(TAG, "Error while logging in user with email and password", e)
            false
        }
    }

    suspend fun register(name: String, email: String, password: String): Boolean {
        return try {
            account.create(
                userId = ID.unique(),
                name = name,
                email = email,
                password = password
            )
            login(email, password)
        } catch (e: AppwriteException) {
            logError(TAG, "Error while registering user with email", e)
            false
        }
    }

    suspend fun googleLoginOrRegister(activity: ComponentActivity) {
        account.createOAuth2Session(
            activity = activity,
            provider = OAuthProvider.GOOGLE,
        )
    }


    suspend fun logout() {
        account.deleteSession("current")
    }

}

