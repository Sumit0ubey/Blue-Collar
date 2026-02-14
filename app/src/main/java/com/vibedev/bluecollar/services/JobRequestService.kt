package com.vibedev.bluecollar.services

import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.services.Databases
import io.appwrite.Permission
import io.appwrite.Role
import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.JobRequest
import com.vibedev.bluecollar.utils.logError
import io.appwrite.Query


class JobRequestService(client: Client) {

    private val databases = Databases(client)
    private val TAG = "JobRequestService"

    suspend fun createRequest(customerId: String, customerName: String, city: String, address: String, serviceDescription: String, serviceType: String) {
        try {
            val perms = listOf(
                Permission.read(Role.user(customerId)),
                Permission.update(Role.user(customerId)),
                Permission.read(Role.team(AppData.MAKE_PROVIDER_FUNCTION_ID))
            )

            databases.createDocument(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.JOB_REQUEST_COLLECTION_ID,
                documentId = ID.unique(),
                data = mapOf(
                    "customerId" to customerId,
                    "name" to customerName,
                    "city" to city,
                    "address" to address,
                    "description" to serviceDescription,
                    "serviceType" to serviceType,
                    "status" to "open"
                ),
                permissions = perms
            )
        } catch (e: Exception) {
            logError(TAG, "Error creating job request for user $customerId", e)
        }
    }

    suspend fun getOpenJobs(city: String? = null, serviceType: String? = null): List<JobRequest> {
        return try {
            val queries = mutableListOf(
                Query.equal("status", "open"),
                Query.orderDesc($$"$createdAt"),
                Query.limit(50)
            )

            city?.let { queries.add(Query.equal("city", it)) }
            serviceType?.let { queries.add(Query.equal("serviceType", it)) }

            val response = databases.listDocuments(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.JOB_REQUEST_COLLECTION_ID,
                queries = queries
            )

            response.documents.map { doc ->
                val data = doc.data
                JobRequest(
                    id = doc.id,
                    serviceType = data["serviceType"] as String,
                    description = data["description"] as String?,
                    city = data["city"] as String?,
                    address = data["address"] as String?,
                    cost = data["cost"] as String?,
                )
            }
        } catch (e: Exception) {
            logError(TAG, "Error getting Jobs for $city and $serviceType ", e)
            emptyList()
        }
    }

    suspend fun updateJobRequestStatus(jobRequestId: String, status: String) {
        try {
            databases.updateDocument(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.JOB_REQUEST_COLLECTION_ID,
                documentId = jobRequestId,
                data = mapOf("status" to status)
            )
        } catch (e: Exception) {
            logError(TAG, "Error updating status for job request $jobRequestId", e)
        }
    }
}
