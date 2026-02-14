package com.vibedev.bluecollar.services

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.data.JobHistory
import com.vibedev.bluecollar.utils.logError
import io.appwrite.Client
import io.appwrite.Query
import io.appwrite.models.Document
import io.appwrite.services.Databases
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class JobHistoryService(client: Client) {
    private val databases = Databases(client)
    private val TAG = "JobHistoryService"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }


    private suspend fun fetchCombinedHistoryDocuments(filterQueries: List<String>): List<Document<Map<String, Any>>> {
        val userId = AppData.authToken
        if (userId.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            val customerJobsResponse = databases.listDocuments(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.JOB_HISTORY_COLLECTION_ID,
                queries = filterQueries + Query.equal("customerId", userId) + Query.equal("isComplete", true)
            )

            val providerJobsResponse = databases.listDocuments(
                databaseId = AppData.DATABASE_ID,
                collectionId = AppData.JOB_HISTORY_COLLECTION_ID,
                queries = filterQueries + Query.equal("serviceProviderId", userId) + Query.equal("isComplete", true)
            )

            (customerJobsResponse.documents + providerJobsResponse.documents)
                .distinctBy { it.id }
        } catch (e: Exception) {
            logError(TAG, e.message.toString())
            emptyList()
        }
    }

    private fun Document<Map<String, Any>>.toJobHistory(): JobHistory {
        val data = this.data
        return JobHistory(
            id = this.id,
            serviceType = data["serviceType"] as String,
            summary = data["summary"] as String,
            cost = "₹" + data["cost"] as String,
        )
    }

    private suspend fun getHistory(
        limit: Int? = null,
        reverseOrder: Boolean = false,
        filterQueries: List<String> = emptyList()
    ): List<JobHistory> {
        val jobDocs = fetchCombinedHistoryDocuments(filterQueries)
        val sortedDocs = if (reverseOrder) {
            jobDocs.sortedBy { it.updatedAt }
        } else {
            jobDocs.sortedByDescending { it.updatedAt }
        }
        val limitedDocs = limit?.let { sortedDocs.take(it) } ?: sortedDocs
        return limitedDocs.map { it.toJobHistory() }
    }

    suspend fun getJobHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        return getHistory(limit, reverseOrder)
    }

    suspend fun getTodaysJobHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = dateFormat.format(calendar.time)
        val queries = listOf(Query.greaterThanEqual($$"$updatedAt", startOfToday))
        return getHistory(limit, reverseOrder, queries)
    }

    suspend fun getThisWeeksJobHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = dateFormat.format(calendar.time)
        val queries = listOf(Query.greaterThanEqual($$"$updatedAt", startOfWeek))
        return getHistory(limit, reverseOrder, queries)
    }

    suspend fun getThisMonthsJobHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = dateFormat.format(calendar.time)
        val queries = listOf(Query.greaterThanEqual($$"$updatedAt", startOfMonth))
        return getHistory(limit, reverseOrder, queries)
    }

    suspend fun getThisYearsJobHistory(limit: Int? = null, reverseOrder: Boolean = false): List<JobHistory> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYear = dateFormat.format(calendar.time)
        val queries = listOf(Query.greaterThanEqual($$"$updatedAt", startOfYear))
        return getHistory(limit, reverseOrder, queries)
    }
}
