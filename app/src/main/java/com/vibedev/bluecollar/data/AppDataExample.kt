package com.vibedev.bluecollar.data

object AppDataExample {

    // Appwrite configuration
    const val APPWRITE_ENDPOINT = "https://region.cloud.appwrite.io/v1" // something like this but instead of this use your endpoint
    const val APPWRITE_PROJECT_ID = "your_project_id"  // example 546ef4545def8
    const val DATABASE_ID = "your_database_id"
    const val PROFILE_COLLECTION_ID = "your_profile_collection_id"
    const val JOB_REQUEST_COLLECTION_ID = "your_job_request_collection_id"
    const val JOB_HISTORY_COLLECTION_ID = "your_job_history_collection_id"
    const val NOTIFICATION_COLLECTION_ID = "your_notification_collection_id"
    const val SERVICE_TYPES_COLLECTION_ID = "your_service_types_collection_id"
    const val SERVICE_DETAILS_COLLECTION_ID = "your_service_details_collection_id"
    const val MAKE_PROVIDER_FUNCTION_ID = "your_make_provider_function_id"


    // Cloudinary configuration
    const val CLOUDINARY_CLOUD_NAME = "your_cloudinary_cloud_name"
    const val CLOUDINARY_UPLOAD_PRESET = "your_upload_preset"


    // Other configuration
    val SERVICE_LOCATIONS = listOf("Bhiwandi", "Dombivli", "Kalyan", "Mumbai", "Thane") // instead of this use your service locations
    var authToken: String? = null
    var userProfile: UserProfile? = null
    var serviceTypes: List<String> = emptyList()
    var services: List<Service> = emptyList()
}