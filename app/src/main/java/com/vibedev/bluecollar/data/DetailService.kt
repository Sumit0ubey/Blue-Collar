package com.vibedev.bluecollar.data

data class DetailService (
    val icon: String,
    val title: String,
    val serviceType: String,
    val summary: String,
    val included: List<String>,
    val portfolio: List<String>,
    val price: String,
)