package com.vibedev.bluecollar.data

sealed class JobUpdate {
    data class OpenJob(val payload: Map<String, Any?>) : JobUpdate()
    data class JobAccepted(val jobId: String) : JobUpdate()
    data class Error(val throwable: Throwable) : JobUpdate()
}