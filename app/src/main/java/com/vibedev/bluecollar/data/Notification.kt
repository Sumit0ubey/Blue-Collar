package com.vibedev.bluecollar.data

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val iconRes: String,
    var isRead: Boolean = false
)