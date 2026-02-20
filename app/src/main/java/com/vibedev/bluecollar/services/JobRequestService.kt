package com.vibedev.bluecollar.services

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.Job
import com.vibedev.bluecollar.data.JobRequest
import com.vibedev.bluecollar.data.JobShort
import com.vibedev.bluecollar.utils.getThisWeekDateRangeISO
import com.vibedev.bluecollar.utils.getTodayDateRangeISO
import com.vibedev.bluecollar.utils.logError
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import io.appwrite.services.Databases
import java.text.SimpleDateFormat
import java.util.Locale

class JobRequestService(client: Client) {

    private val databases = Databases(client)
    private val tag = "JobRequestService"

    suspend fun createRequest(customerId: String, customerName: String, city: String, address: String, serviceDescription: String, serviceType: String, pay: String) {
        try {
            val perms = listOf(
                Permission.read(Role.user(customerId)),
                Permission.update(Role.user(customerId)),
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
                    "cost" to pay,
                    "status" to "open"
                ),
                permissions = perms
            )
        } catch (e: Exception) {
            logError(tag, "Error creating job request for user $customerId", e)
        }
    }

    suspend fun getJobById(jobId: String): Job? {
        val sdfISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        return try {
            val doc = databases.getDocument(
                AppData.DATABASE_ID,
                AppData.JOB_REQUEST_COLLECTION_ID,
                jobId
            )

            val data = doc.data
            Job(
                id = doc.id,
                customerId = data["customerId"] as String,
                customerName = data["name"] as String,
                customerPhoneNumber = data["number"] as? String ?: "",
                providerId = data["assignedProviderId"] as? String ?: "",
                providerName = data["assignedProviderName"] as? String ?: "",
                providerNumber = data["assignedProviderNumber"] as? String ?: "",
                description = data["description"] as String,
                serviceType = data["serviceType"] as String,
                city = data["city"] as String,
                address = data["address"] as String,
                cost = data["cost"] as String,
                status = data["status"] as String,
                date = sdfISO.parse(doc.updatedAt)
            )

        }catch (e: Exception){
            logError(tag, "Error getting job by id $jobId", e)
            null
        }
    }

    suspend fun getOpenJobs(city: String? = null, serviceType: String? = null): List<JobRequest> {
        return try {
            val queries = mutableListOf(
                Query.equal("status", AppData.OPEN),
                Query.orderDesc($$"$updatedAt"),
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
            logError(tag, "Error getting Jobs for $city and $serviceType ", e)
            emptyList()
        }
    }

    private suspend fun getJobs(baseQueries: List<String>): List<JobShort> {
        val userId = AppData.authToken ?: run {
            logError(tag, "User profile not available.")
            return emptyList()
        }

        val customerQueries = baseQueries + Query.equal("customerId", userId)
        val providerQueries = baseQueries + Query.equal("assignedProviderId", userId)

        val jobsAsCustomer = fetchJobsWithQueries(customerQueries, "customer", userId)
        val jobsAsProvider = fetchJobsWithQueries(providerQueries, "provider", userId)

        val allJobDocuments = (jobsAsCustomer + jobsAsProvider).distinctBy { it.id }

        return mapDocumentsToJobs(allJobDocuments).sortedByDescending { it.date }
    }

    private suspend fun fetchJobsWithQueries(queries: List<String>, role: String, userId: String): List<io.appwrite.models.Document<Map<String, Any>>> {
        return try {
            databases.listDocuments(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.JOB_REQUEST_COLLECTION_ID,
                queries = queries
            ).documents
        } catch (e: Exception) {
            logError(tag, "Error fetching jobs as $role for user $userId", e)
            emptyList()
        }
    }

    private fun mapDocumentsToJobs(documents: List<io.appwrite.models.Document<Map<String, Any>>>): List<JobShort> {
        val sdfISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        return documents.mapNotNull { doc ->
            try {
                val data = doc.data
                JobShort(
                    id = doc.id,
                    description = data["description"] as String,
                    serviceType = data["serviceType"] as String,
                    cost = data["cost"] as String,
                    status = data["status"] as String,
                    date = sdfISO.parse(doc.updatedAt)
                )
            } catch (e: Exception) {
                logError(tag, "Failed to map document ${doc.id} to Job object", e)
                null
            }
        }
    }

    suspend fun getCurrentJobRequests(): List<JobShort> {
        return getJobs(listOf(
            Query.equal("status", listOf(AppData.OPEN, AppData.ACCEPTED)),
            Query.orderDesc($$"$updatedAt")
        ))
    }

    suspend fun getPreviousJobsRequestOfToday(): List<JobShort> {
        val (start, end) = getTodayDateRangeISO()
        return getJobs(
            listOf(
                Query.notEqual("status", AppData.OPEN),
                Query.notEqual("status", AppData.ACCEPTED),
                Query.between($$"$updatedAt", start, end),
                Query.orderDesc($$"$updatedAt")
            )
        )
    }

    suspend fun getPreviousJobsRequestOfThisWeek(): List<JobShort> {
        val (start, end) = getThisWeekDateRangeISO()
        return getJobs(
            listOf(
                Query.notEqual("status", AppData.OPEN),
                Query.notEqual("status", AppData.ACCEPTED),
                Query.between($$"$updatedAt", start, end),
                Query.orderDesc($$"$updatedAt")
            )
        )
    }

    suspend fun updateJobStatus(jobId: String, newStatus: String) {
        try {
            databases.updateDocument(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.JOB_REQUEST_COLLECTION_ID,
                documentId = jobId,
                data = mapOf(
                    "status" to newStatus,
                    "isComplete" to (newStatus == AppData.COMPLETED)
                )
            )
        }catch (e: Exception){
            logError(tag, "Error updating job status for job $jobId", e)
        }
    }
}
