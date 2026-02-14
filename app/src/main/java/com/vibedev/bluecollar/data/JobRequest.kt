package com.vibedev.bluecollar.data

data class JobRequest(
    val id: String,
    val serviceType: String,
    val description: String?,
    val city: String?,
    val address: String?,
    val cost: String?
)