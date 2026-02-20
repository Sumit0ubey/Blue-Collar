package com.vibedev.bluecollar.data

import java.util.Date

data class JobShort(
    val id: String,
    val serviceType: String,
    val description: String,
    val status: String,
    val cost: String,
    val date: Date?,
)