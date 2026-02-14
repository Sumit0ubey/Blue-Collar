package com.vibedev.bluecollar.viewModels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import com.vibedev.bluecollar.manager.AppwriteManager

class AuthViewModel : ViewModel() {

    suspend fun getCurrentUser() =
        AppwriteManager.account.getLoggedIn()

    suspend fun login(email: String, password: String): Boolean {
        return AppwriteManager.account.login(email, password)
    }

    suspend fun googleLogin(activity: ComponentActivity) {
        AppwriteManager.account.googleLoginOrRegister(activity)
    }

    suspend fun register(name: String, email: String, password: String): Boolean {
        return AppwriteManager.account.register(name, email, password)
    }

    suspend fun logout(){
        AppwriteManager.account.logout()
    }

}