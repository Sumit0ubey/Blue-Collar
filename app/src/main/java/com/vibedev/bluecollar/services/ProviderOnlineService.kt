package com.vibedev.bluecollar.services

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.content.Intent
import kotlinx.coroutines.Job

import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.data.JobUpdate
import com.vibedev.bluecollar.manager.AppwriteManager
import com.vibedev.bluecollar.ui.notification.IncomingJobNotifications


class ProviderOnlineService : LifecycleService() {

    private val tag = "ProviderOnlineService"
    private var collectJob: Job? = null

    companion object {
        const val ACTION_REALTIME_SUBSCRIPTION_SUCCESS =
            "com.vibedev.bluecollar.REALTIME_SUBSCRIPTION_SUCCESS"
        const val ACTION_REALTIME_SUBSCRIPTION_ERROR =
            "com.vibedev.bluecollar.REALTIME_SUBSCRIPTION_ERROR"
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, IncomingJobNotifications.buildOngoingOnlineNotification(this))
        startRealtime()
    }

    private fun sendUiBroadcast(action: String) {
        sendBroadcast(Intent(action).apply { setPackage(packageName) })
    }

    private fun startRealtime() {
        collectJob?.cancel()
        collectJob = lifecycleScope.launch {
            try {
                sendUiBroadcast(ACTION_REALTIME_SUBSCRIPTION_SUCCESS)

                AppwriteManager.realtime.subscribeToJobs().collect { update ->
                    when (update) {
                        is JobUpdate.OpenJob -> {
                            val jobId = update.payload["\$id"] as? String ?: return@collect
                            IncomingJobNotifications.showIncomingFullScreen(
                                this@ProviderOnlineService, jobId, update.payload
                            )
                        }
                        is JobUpdate.JobAccepted -> {
                            NotificationManagerCompat.from(this@ProviderOnlineService)
                                .cancel(update.jobId.hashCode())
                        }
                        is JobUpdate.Error -> {
                            logError(tag, "Error in realtime collection",
                                update.throwable as Exception?
                            )
                            sendUiBroadcast(ACTION_REALTIME_SUBSCRIPTION_ERROR)
                            stopSelf()
                        }
                    }
                }
            } catch (e: Exception) {
                logError(tag, "Error subscribing to realtime service", e)
                sendUiBroadcast(ACTION_REALTIME_SUBSCRIPTION_ERROR)
                stopSelf()
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        collectJob?.cancel()
        super.onDestroy()
    }
}
