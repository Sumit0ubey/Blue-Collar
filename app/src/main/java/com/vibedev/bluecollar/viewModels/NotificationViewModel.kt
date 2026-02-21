package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibedev.bluecollar.data.Notification
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.utils.logError
import kotlinx.coroutines.Dispatchers
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
                _notifications.postValue(Result.failure(e))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updateNotificationToNotShow(notificationId: String) {
        val currentList = _notifications.value?.getOrNull()?.toMutableList()
        currentList?.let {
            val updatedList = it.filterNot { notification -> notification.id == notificationId }
            _notifications.postValue(Result.success(updatedList))
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                AppwriteManager.notification.updateNotificationToNotShow(notificationId, false)
            } catch (e: Exception) {
                logError("NotificationViewModel", "Error updating notification to not show: $notificationId", e)
            }
        }
    }

    fun updateNotificationToRed(notificationId: String) {
        val currentList = _notifications.value?.getOrNull()?.toMutableList()
        currentList?.let { list ->
            val index = list.indexOfFirst { it.id == notificationId }
            if (index != -1) {
                val notification = list[index]
                if (!notification.isRead) {
                    val updatedNotification = notification.copy(isRead = true)
                    list[index] = updatedNotification
                    _notifications.postValue(Result.success(list))
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                AppwriteManager.notification.updateNotificationToRed(notificationId, true)
            } catch (e: Exception) {
                logError("NotificationViewModel", "Error updating notification to read: $notificationId", e)
            }
        }
    }
}
