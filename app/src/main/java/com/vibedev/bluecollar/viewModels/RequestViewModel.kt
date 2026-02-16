package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.ViewModel

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.JobRequest
import com.vibedev.bluecollar.manager.AppwriteManager

class RequestViewModel : ViewModel() {
    
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

    suspend fun updateJobRequestStatus(jobRequestId: String, status: String) {
        return AppwriteManager.request.updateJobRequestStatus(jobRequestId, status)
    }
}