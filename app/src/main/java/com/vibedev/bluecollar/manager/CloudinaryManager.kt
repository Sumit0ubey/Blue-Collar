package com.vibedev.bluecollar.manager

import android.content.Context
import com.vibedev.bluecollar.data.AppData
import com.cloudinary.android.MediaManager


object CloudinaryManager {
    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to AppData.CLOUDINARY_CLOUD_NAME,
        )
        MediaManager.init(context, config)
    }
}
