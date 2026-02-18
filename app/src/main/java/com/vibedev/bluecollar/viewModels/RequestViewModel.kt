package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.ViewModel
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.Job
import com.vibedev.bluecollar.data.JobRequest
import com.vibedev.bluecollar.manager.AppwriteManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RequestViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun createRequest(customerName: String, number: String, city: String, address: String, serviceDescription: String, serviceType: String, pay: String, isFunction: Boolean = true) {
        if (isFunction) {
            createRequestViaFunction(customerName, number, city, address, serviceDescription, serviceType, pay)
        } else {
            createRequestViaCollection(AppData.authToken ?: "", customerName, number, city, address, serviceDescription, serviceType)
        }
    }

    suspend fun createRequestViaCollection(customerId: String, customerName: String, city: String, address: String, serviceDescription: String, serviceType: String, pay: String) {
        return AppwriteManager.request.createRequest(customerId, customerName, city, address, serviceDescription, serviceType, pay)
    }

    suspend fun createRequestViaFunction(customerName: String, number: String, city: String, address: String, serviceDescription: String, serviceType: String, pay: String): Boolean {
        return AppwriteManager.functions.createRequestViaFunction(customerName, number, city, address, serviceDescription, serviceType, pay)
    }

    suspend fun getOpenJobs(city: String? = null, serviceType: String? = null): List<JobRequest> {
        return AppwriteManager.request.getOpenJobs(city, serviceType)
    }

    suspend fun cancelJobRequest(jobId: String) {
        _isLoading.value = true
        try {
            AppwriteManager.request.updateJobStatus(jobId, AppData.CANCELLED)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun markJobAsComplete(jobId: String) {
        _isLoading.value = true
        try {
            AppwriteManager.request.updateJobStatus(jobId, AppData.COMPLETED)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun getCurrentRequests(): List<Job> {
        return AppwriteManager.request.getCurrentJobRequests()
    }

    suspend fun getPreviousRequestOfToday(): List<Job> {
        return AppwriteManager.request.getPreviousJobsRequestOfToday()
    }

    suspend fun getPreviousRequestOfThisWeek(): List<Job> {
        return AppwriteManager.request.getPreviousJobsRequestOfThisWeek()
    }
}