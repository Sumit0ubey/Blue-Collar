package com.vibedev.bluecollar.services

import com.vibedev.bluecollar.utils.logError
import com.vibedev.bluecollar.utils.logInfo
import com.vibedev.bluecollar.data.AppData

import io.appwrite.Client
import org.json.JSONObject
import java.lang.Exception
import io.appwrite.services.Functions

class FunctionsService(client: Client) {

    private val functions = Functions(client)
    private val TAG = "FunctionService"

    suspend fun callMakeProvider(userId: String): Boolean {
        return try {
            val body = JSONObject()
                .put("userId", userId)
                .toString()

            val execution = functions.createExecution(
                functionId = AppData.MAKE_PROVIDER_FUNCTION_ID,
                body = body,
                async = true
            )

            val responseJson = JSONObject(execution.responseBody)
            if (responseJson.optBoolean("ok") && responseJson.optBoolean("added")) {
                val teamId = responseJson.optString("teamId")
                val membershipId = responseJson.optString("membershipId")
                logInfo(TAG, "User successfully made a provider. TeamId: $teamId, MembershipId: $membershipId")
                true
            } else {
                logError(TAG, "Failed to make provider. Response: ${execution.responseBody}")
                false
            }
        } catch (e: Exception) {
            logError(TAG, "Error calling make provider function for user $userId", e)
            false
        }
    }
}