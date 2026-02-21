package com.vibedev.bluecollar.services

import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.services.Databases

import com.vibedev.bluecollar.data.DetailService
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.Service
import com.vibedev.bluecollar.utils.logError


class ServicesService(client: Client) {

    private val DATABASE_ID = AppData.DATABASE_ID
    private val SERVICE_DETAILS_COLLECTION_ID = AppData.SERVICE_DETAILS_COLLECTION_ID
    private val SERVICE_TYPES_COLLECTION_ID = AppData.SERVICE_TYPES_COLLECTION_ID
    
    private val databases = Databases(client)
    private val TAG = "ServicesService"

    suspend fun doesServiceDetailExist(serviceId: String): Boolean {
        return try {
            databases.getDocument(
                DATABASE_ID,
                SERVICE_DETAILS_COLLECTION_ID,
                serviceId
            )
            true
        } catch (e: Exception) {
            logError(TAG, "Error checking if Service detail exists for $serviceId", e)
            false
        }
    }


    suspend fun getServiceDetail(serviceId: String): DetailService? {
        return try {
            val doc = databases.getDocument(
                DATABASE_ID,
                SERVICE_DETAILS_COLLECTION_ID,
                serviceId
            )

            val data = doc.data
            if (data["isActive"] as? Boolean == true) {
                DetailService(
                    icon = data["iconPic"] as String,
                    title = data["title"] as String,
                    serviceType = data["serviceType"] as String,
                    summary = data["description"] as String,
                    included = data["included"] as List<String>,
                    portfolio = data["portfolioPic"] as List<String>,
                    price = data["startPrice"] as String,
                )
            } else {
                null
            }
        } catch (e: Exception) {
            logError(TAG, "Error getting service detail for $serviceId", e)
            null
        }
    }

    suspend fun getService(): List<Service> {
        return try {
            val response = databases.listDocuments(
                DATABASE_ID,
                SERVICE_DETAILS_COLLECTION_ID,
                listOf(Query.equal("isActive", true))
            )
            response.documents.map { doc ->
                val data = doc.data
                Service(
                    icon = data["iconPic"] as String,
                    serviceID = doc.id,
                    title = data["title"] as String,
                    description = data["description"] as String,
                    price = data["startPrice"] as String,
                )
            }
        } catch (e: Exception) {
            logError(TAG, "Error getting services", e)
            emptyList()
        }
    }

    suspend fun getServicesByServiceType(serviceTypeName: String): List<Service> {
        return try {
            val response = databases.listDocuments(
                DATABASE_ID,
                SERVICE_DETAILS_COLLECTION_ID,
                listOf(
                    Query.equal("isActive", true),
                    Query.equal("serviceType", serviceTypeName)
                )
            )
            response.documents.map { doc ->
                val data = doc.data
                Service(
                    icon = data["iconPic"] as String,
                    serviceID = doc.id,
                    title = data["title"] as String,
                    description = data["description"] as String,
                    price = data["startPrice"] as String,
                )
            }
        } catch (e: Exception) {
            logError(TAG, "Error getting services for service type $serviceTypeName", e)
            emptyList()
        }
    }

    suspend fun getServicesByCity(city: String): List<Service> {
        return try {
            val response = databases.listDocuments(
                DATABASE_ID,
                SERVICE_DETAILS_COLLECTION_ID,
                listOf(
                    Query.equal("isActive", true),
                    Query.equal("city", city)
                )
            )
            response.documents.map { doc ->
                val data = doc.data
                Service(
                    icon = data["iconPic"] as String,
                    serviceID = doc.id,
                    title = data["title"] as String,
                    description = data["description"] as String,
                    price = data["startPrice"] as String,
                )
            }
        } catch (e: Exception) {
            logError(TAG, "Error getting services for city $city", e)
            emptyList()
        }
    }

    suspend fun getServiceTypeNames(): List<String> {
        return try {
            val response = databases.listDocuments(
                DATABASE_ID,
                SERVICE_TYPES_COLLECTION_ID,
                listOf(Query.equal("isActive", true))
            )
            response.documents.map { doc ->
                doc.data["name"] as String
            }.sorted()
        } catch (e: Exception) {
            logError(TAG, "Error getting service types", e)
            emptyList()
        }
    }

    suspend fun getServiceCity(): List<String> {
        return try {
            val response = databases.listDocuments(
                DATABASE_ID,
                AppData.CITY_COLLECTION_ID,
                listOf(Query.equal("isActive", true))
            )

            response.documents.map { doc ->
                doc.data["name"] as String
            }.sorted()

        } catch (e: Exception){
            logError(TAG, "Error getting city ", e)
            emptyList()
        }
    }
}
