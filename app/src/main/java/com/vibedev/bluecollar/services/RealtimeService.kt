package com.vibedev.bluecollar.services

import io.appwrite.models.RealtimeSubscription
import io.appwrite.services.Realtime
import io.appwrite.Client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

import com.vibedev.bluecollar.data.JobUpdate
import com.vibedev.bluecollar.data.AppData


class RealtimeService(client: Client) {

    private val realtime: Realtime = Realtime(client)
    private val dbId = AppData.DATABASE_ID
    private val jobsColId = AppData.JOB_REQUEST_COLLECTION_ID

    private val fieldStatus = "status"
    private val fieldCity = "city"
    private val fieldService = "serviceType"

    fun subscribeToJobs(): Flow<JobUpdate> = callbackFlow {

        val channel = "databases.$dbId.collections.$jobsColId.documents"
        var subscription: RealtimeSubscription? = null

        try {
            subscription = realtime.subscribe((channel)) { message ->
                try {
                    val payload = message.payload as? Map<String, Any?> ?: return@subscribe
                    val events = message.events ?: emptyList()

                    val isCreateOrUpdate = events.any { it.endsWith(".create") || it.endsWith(".update") }
                    if (!isCreateOrUpdate) return@subscribe

                    val jobId = payload[$$"$id"] as? String ?: return@subscribe
                    val customerId = payload["customerId"] as String
                    val statusRaw = payload[fieldStatus] as? String ?: return@subscribe
                    val status = statusRaw.trim().lowercase()

                    if (status == "accepted") {
                        trySend(JobUpdate.JobAccepted(jobId))
                        return@subscribe
                    }

                    if (status == "open") {
//                        val city = payload[fieldCity] as? String ?: return@subscribe
//                        val type = payload[fieldService] as? String ?: return@subscribe

                        if (customerId != AppData.authToken) {
                            trySend(JobUpdate.OpenJob(payload))
                        }
                    }
                } catch (t: Throwable) {
                    trySend(JobUpdate.Error(t))
                }
            }
        } catch (t: Throwable) {
            trySend(JobUpdate.Error(t))
        }

        awaitClose {
            subscription?.close()
        }
    }
}
