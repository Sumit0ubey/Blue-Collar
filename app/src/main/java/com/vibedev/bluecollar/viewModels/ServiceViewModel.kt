package com.vibedev.bluecollar.viewModels

import androidx.lifecycle.ViewModel

import com.vibedev.bluecollar.data.Service
import com.vibedev.bluecollar.data.DetailService
import com.vibedev.bluecollar.manager.AppwriteManager

class ServiceViewModel : ViewModel() {

    suspend fun getServiceTypeNames(): List<String> {
        return AppwriteManager.services.getServiceTypeNames()
    }

    suspend fun doesServiceDetailExist(serviceId: String): Boolean {
        return AppwriteManager.services.doesServiceDetailExist(serviceId)
    }

    suspend fun getServiceDetail(serviceId: String): DetailService? {
        return AppwriteManager.services.getServiceDetail(serviceId)
    }

    suspend fun getService(): List<Service> {
        return AppwriteManager.services.getService()
    }

    suspend fun getServicesByServiceType(serviceTypeName: String): List<Service> {
        return AppwriteManager.services.getServicesByServiceType(serviceTypeName)
    }

    suspend fun getServicesByCity(city: String): List<Service> {
        return AppwriteManager.services.getServicesByCity(city)
    }

}