package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.ViewModel
import com.vibedev.bluecollar.data.JobRequest
import com.vibedev.bluecollar.manager.AppwriteManager

class RequestViewModel : ViewModel() {

    suspend fun createRequest(customerId: String, customerName: String, city: String, address: String, serviceDescription: String, serviceType: String) {
        return AppwriteManager.request.createRequest(customerId, customerName, city, address, serviceDescription, serviceType)
    }

    suspend fun getOpenJobs(city: String? = null, serviceType: String? = null): List<JobRequest> {
        return AppwriteManager.request.getOpenJobs(city, serviceType)
    }

    suspend fun updateJobRequestStatus(jobRequestId: String, status: String) {
        return AppwriteManager.request.updateJobRequestStatus(jobRequestId, status)
    }
}