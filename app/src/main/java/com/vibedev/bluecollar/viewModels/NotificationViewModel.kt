package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibedev.bluecollar.data.Notification
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.utils.logError
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableLiveData<Result<List<Notification>>>()
    val notifications: LiveData<Result<List<Notification>>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val notificationList = AppwriteManager.notification.getNotifications()
                _notifications.postValue(Result.success(notificationList ?: emptyList()))
            } catch (e: Exception) {
                logError("NotificationViewModel", "Error fetching notifications", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}