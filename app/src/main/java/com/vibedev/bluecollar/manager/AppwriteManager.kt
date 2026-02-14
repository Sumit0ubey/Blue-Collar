package com.vibedev.bluecollar.manager

import android.content.Context
import io.appwrite.Client

import com.vibedev.bluecollar.data.AppData
import com.vibedev.bluecollar.services.ProfileService
import com.vibedev.bluecollar.services.AccountService
import com.vibedev.bluecollar.services.ServicesService
import com.vibedev.bluecollar.services.FunctionsService
import com.vibedev.bluecollar.services.JobHistoryService
import com.vibedev.bluecollar.services.JobRequestService
import com.vibedev.bluecollar.services.NotificationService


object AppwriteManager {
    private const val ENDPOINT = AppData.APPWRITE_ENDPOINT
    private const val PROJECT_ID = AppData.APPWRITE_PROJECT_ID
    private lateinit var client: Client
    internal lateinit var account: AccountService
    internal lateinit var profile: ProfileService
    internal lateinit var services: ServicesService
    internal lateinit var request: JobRequestService
    internal lateinit var requestHistory: JobHistoryService
    internal lateinit var notification: NotificationService
    internal lateinit var functions: FunctionsService


    fun init(context: Context) {
        client = Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)

        account = AccountService(client)
        profile = ProfileService(client)
        services = ServicesService(client)
        request = JobRequestService(client)
        requestHistory = JobHistoryService(client)
        notification = NotificationService(client)
        functions = FunctionsService(client)
    }

}