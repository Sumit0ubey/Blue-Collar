package com.vibedev.bluecollar.data

import java.util.Date

data class Job(
    val id: String,
    val serviceType: String,
    val description: String,
    val customerId: String,
    val customerName: String,
    val customerPhoneNumber: String,
    val status: String,
    val providerId: String,
    val providerName: String,
    val providerNumber: String,
    val address: String,
    val cost: String,
    val city: String,
    val date: Date?,
)
