package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.ViewModel
import com.vibedev.bluecollar.data.UserProfile
import com.vibedev.bluecollar.manager.AppwriteManager

class ProfileViewModel : ViewModel() {

    suspend fun doesProfileExist(userId: String): Boolean {
        return AppwriteManager.profile.doesProfileExist(userId)
    }

    suspend fun getProfile(userId: String): UserProfile? {
        return AppwriteManager.profile.getProfile(userId)
    }

    suspend fun createProfile(userId: String, name: String, phone: String, city: String, isServiceProvider: Boolean, serviceType: String, address: String, image: String) {
        AppwriteManager.profile.createProfile(
            userId = userId,
            userName = name,
            userNumber = phone,
            userCity = city,
            userAddress = address,
            isServiceProvider = isServiceProvider,
            serviceType = serviceType,
            profileImage = image
        )

        if (isServiceProvider) {
            AppwriteManager.functions.callMakeProvider(userId)
        }
    }

    suspend fun updateProfile(userId: String, data: Map<String, Any?>) {
        AppwriteManager.profile.updateProfile(userId, data)
    }
}
