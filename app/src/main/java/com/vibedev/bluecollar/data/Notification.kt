package com.vibedev.bluecollar.data

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val iconRes: Int,
    var isRead: Boolean = false
)