package com.vibedev.bluecollar.data

data class UserProfile(
    val name: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val address: String? = null,
    val isServiceProvider: Boolean,
    val serviceType: String? = null,
    val profileImage: String? = null,
    val rating: String? = null,
    val about: String? = null,
    val portfolio: List<String>? = null
)
