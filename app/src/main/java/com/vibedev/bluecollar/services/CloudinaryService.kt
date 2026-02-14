package com.vibedev.bluecollar.services

import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.MediaManager
import android.net.Uri

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.utils.logDebug
import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.utils.logWarning

object CloudinaryService {

    private const val TAG = "CloudinaryService"

    fun uploadToCloudinary(uri: Uri?, onComplete: (String) -> Unit) {
        if (uri == null) {
            onComplete("")
            return
        }
        MediaManager.get()
            .upload(uri)
            .unsigned(AppData.CLOUDINARY_UPLOAD_PRESET)
            .callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                logDebug(TAG, "Upload started for request: $requestId")
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {

            }

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val url = resultData["url"].toString().replace("http", "https")
                logDebug(TAG, "Upload success for request $requestId. URL: $url")
                onComplete(url)
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                logError(TAG, "Upload error for request $requestId: ${error.description}")
                onComplete("")
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {
                logWarning(TAG, "Upload rescheduled for request $requestId: ${error.description}")
            }
        }).dispatch()
    }

    fun uploadImages(uris: List<Uri>, onComplete: (List<String>) -> Unit) {
        if (uris.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val uploadedUrls = mutableListOf<String>()
        val totalImages = uris.size
        var uploadedCount = 0

        uris.forEach { uri ->
            uploadToCloudinary(uri) { imageUrl ->
                uploadedCount++
                if (imageUrl.isNotEmpty()) {
                    uploadedUrls.add(imageUrl)
                }
                if (uploadedCount == totalImages) {
                    onComplete(uploadedUrls)
                }
            }
        }
    }
}
