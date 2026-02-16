package com.vibedev.bluecollar.viewModels

import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vibedev.bluecollar.data.JobUpdate
import com.vibedev.bluecollar.manager.AppwriteManager


class RealtimeViewModel : ViewModel() {

    private val _jobUpdates = kotlinx.coroutines.flow.MutableSharedFlow<JobUpdate>(
        extraBufferCapacity = 64
    )
    val jobUpdates = _jobUpdates

    fun startRealtime() {
        viewModelScope.launch {
            AppwriteManager.realtime.subscribeToJobs()
                .collect { update ->
                    _jobUpdates.tryEmit(update)
                }
        }
    }
}